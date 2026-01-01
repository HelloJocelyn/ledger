# 支付 + 量化分析一体化平台｜从 0 到 1 落地路线图（v1）

> 目标：**用最小完整闭环（MVP）**搭起可演示、可扩展、能练到关键面试点的系统；随后逐步加难度覆盖分布式、数据分析与风控。

---

## 0. 指导思想（保证能做完 & 好展示）

* 先本地跑通（Docker Compose），后云上（AWS）。
* 单仓 Monorepo（前后 + 回测 + Infra），便于统一版本与脚本。
* **双重记账 + 幂等** 是支付心脏；**事件总线** 是两层系统的桥。
* 一切以**可演示场景**驱动：充值→支付→账本→事件→实时指标→Dashboard。

---

## 1. MVP 范围（两周可交付）

**业务闭环**：

1. 用户注册/登录（简化为邮箱验证码或伪登录）；
2. 充值（mock 支付渠道）→ 入账；
3. 消费（商户扣款）→ 余额扣减；
4. 账本（双记账）与交易查询；
5. 事件投递（Kafka/Redpanda）→ 实时计算**日交易额/活跃用户数**；
6. 前端 Dashboard 展示**余额曲线、当日交易额、Top 商户**。

**非功能**：

* 幂等：`Idempotency-Key` 请求头 + `idempotency_keys` 表。
* 风控（MVP）：单用户/单日限额 + 黑名单校验（内存缓存 + 表）。
* 市场数据：先用**本地 CSV**（ETF 收盘价样例），后接 AlphaVantage（可替换为 yfinance 本地抓取）。

---

## 2. 架构 v1（本地开发形态）

```
[React/Next.js Dashboard]
         |
   [Spring Boot API]
         |  \
      [Postgres]  [Redis]
         |
   [Outbox Table]
         |
    [Outbox Poller]
         |
     [Kafka/Redpanda]  --->  [Stream Worker (Java/Kotlin)]
                                 |-- 聚合指标入 [Postgres (analytics schema)]
                                 |-- 写入 [S3/MinIO] 供离线回测
```

> 说明：支付 API 内同步写业务表 + ledger + outbox，一致性由 **Transactional Outbox** 保证；独立 Poller 将事件投递到 Kafka。

---

## 3. 数据模型（核心表）

**3.1 账户与账本（双重记账）**

* `accounts(id, user_id, type[WALLET|MERCHANT|SYSTEM], balance_minor, currency)`
* `ledger_entries(id, txn_id, posting_time, description)`
* `ledger_postings(id, entry_id, account_id, direction[DEBIT|CREDIT], amount_minor)`
* 约束：每条 `entry` 下 `sum(CREDIT)=sum(DEBIT)`；通过触发器或应用层校验。

**3.2 交易与幂等**

* `transactions(id, user_id, merchant_id, type[TOPUP|PAY|TRANSFER], amount_minor, status[PENDING|SUCCESS|FAILED], created_at)`
* `idempotency_keys(key, request_hash, response_body, status, created_at)`

**3.3 风控**

* `risk_limits(user_id, daily_limit_minor, single_limit_minor)`
* `risk_blacklist(user_id|ip|device_id)`

**3.4 分析侧（明细与聚合）**

* `events_payments(txn_id, user_id, merchant_id, amount_minor, ts)`
* `agg_metrics(day, dau, total_amount_minor, top_merchants jsonb)`

> **金额统一使用最小货币单位**（如日元: 1 = 1JPY；带小数的币种用“分”/“cents”）。

---

## 4. API 设计（片段｜OpenAPI 风格）

```yaml
POST /v1/wallets/{userId}/topup
  headers:
    Idempotency-Key: string
  body: { amount_minor: long }
  200: { txn_id: string, balance_minor: long }

POST /v1/payments
  headers:
    Idempotency-Key: string
  body: { user_id, merchant_id, amount_minor }
  200: { txn_id, status }

GET /v1/ledger/transactions?user_id=...&page=...&size=...
  200: { items: [ ... ], nextCursor: ... }

GET /v1/metrics/today
  200: { dau: int, total_amount_minor: long, top_merchants: [{merchant_id, amount_minor}] }
```

**错误码**：`RISK_LIMIT_EXCEEDED`, `INSUFFICIENT_FUNDS`, `IDEMPOTENT_REPLAY`, `BLACKLISTED`。

---

## 5. 事件模型（Kafka Topic & Schema）

* Topic：`payments.events.v1`
* Key：`txn_id`
* Value(JSON)：

```json
{
  "txn_id": "uuid",
  "type": "TOPUP|PAY|TRANSFER",
  "user_id": "uuid",
  "merchant_id": "uuid",
  "amount_minor": 1200,
  "currency": "JPY",
  "ts": 1730889600000
}
```

* 事件来源：Transactional Outbox 表 `outbox_events(id, aggregate_type, aggregate_id, payload, created_at, published)`

---

## 6. 本地开发环境（Docker Compose）

* **Postgres 16**（主库 + `analytics` schema）
* **Redis**（令牌桶限流/风控缓存）
* **Redpanda**（Kafka 兼容，运维简单）+ Redpanda Console
* **MinIO**（S3 兼容，用于离线回测数据存储）

> 脚本：`make up`, `make down`, `make migrate`, `make seed`。

---

## 7. 代码仓结构（Monorepo）

```
repo/
  backend/
    api/           # Spring Boot (Java), Web + JPA/MyBatis + Flyway
    market-stream/ # 模拟/接收市场行情和成交数据，把它们作为“事件流”推送出去
    quant-engine/  # 订阅事件流，在内存里做一些简单指标（如移动平均），给出简单的策略信号（BUY/SELL/HOLD），对外提供查询接口
    risk-engine/           # 可选模块：规则引擎（Drools/简化版）
    shared/                # DTO、错误码、事件 schema
  analytics/
    batch/                 # Spark/Plain Java + CSV/Parquet 读写
    backtest/              # Python/Java：回测引擎（后续）
  frontend/
    dashboard/             # Next.js/React + Recharts
  infra/
    docker-compose.yml
    flyway/
      V1__init.sql
      V2__ledger.sql
    k8s/                   # 未来上云
  makefile
  README.md
```

---

## 8. 关键实现要点（含伪代码/SQL）

**8.1 事务性出账（支付）**

* 乐观锁：`accounts.balance_version`（或 `SELECT ... FOR UPDATE` 行锁）。
* 流程：检查风控→扣余额→写 `transactions`→写 `ledger_entries + postings`→写 `outbox_events`；一次 DB 事务提交。

**8.2 幂等**

* 收到 `Idempotency-Key`：

    * 若不存在：开始事务，执行业务，持久化响应到 `idempotency_keys`，提交；
    * 若已存在：直接返回存储的响应，状态码 200。

**8.3 双重记账样例（SQL 片段）**

```sql
-- one entry, two postings
insert into ledger_entries(id, txn_id, posting_time, description) values(:eid, :txn, now(), 'PAY');
insert into ledger_postings(id, entry_id, account_id, direction, amount_minor) values
  (:p1, :eid, :user_wallet, 'CREDIT', :amt),
  (:p2, :eid, :merchant_settlement, 'DEBIT', :amt);
```

**8.4 Outbox Poller（简化伪代码）**

```java
while(true){
  List<Outbox> batch = repo.findUnpublished(100);
  for(Outbox e: batch){
    kafka.send("payments.events.v1", e.aggregateId, e.payload);
    repo.markPublished(e.id);
  }
  sleep(200);
}
```

---

## 9. Dashboard（最小可视）

* 指标卡片：今日交易额、DAU、交易笔数；
* 折线：钱包余额、日交易额（7/30 日）；
* 表格：Top 商户；
* 交易明细分页表（可按用户/商户筛选）。

---

## 10. 路线图（6 周执行计划｜从 2025-10-07 起）

**W1：基础骨架**

* 初始化 Monorepo；Spring Boot（payment-api）+ Flyway；Postgres/Redis/Redpanda/MinIO Compose；账户/交易/账本表迁移。

**W2：充值 & 账本**

* 充值 API + 幂等键；双重记账落账；Outbox 表 + Poller；事件发布；简单 Dashboard 初版（静态）。

**W3：消费 & 风控**

* 消费 API（余额扣减 + 风控校验 + 限额）；Stream Worker 写 `agg_metrics`；Dashboard 接入实时指标。

**W4：分析与留存**

* 事件明细落 `events_payments`；Top 商户/留存统计；导出 Parquet→MinIO；Dashboard 增加筛选。

**W5：市场数据与模拟**

* 导入 CSV 行情；资金池“10% 投 ETF”模拟；指标：收益曲线/最大回撤（离线批处理）。

**W6：打磨与演示**

* 错误码与风控规则完善；README + 演示脚本；添加 10~20 条集成测试；录制 3 分钟 Demo。

---

## 11. 上云计划（可选，后置）

* DB：Aurora Postgres；
* 事件：Kinesis（或 MSK）+ Lambda（Outbox Publisher 替换为 EventBridge）；
* 文件：S3；
* 计算：ECS Fargate / EKS；
* 监控：CloudWatch + X-Ray；
* IaC：Terraform 最小栈（VPC、RDS、MSK、S3）。

---

## 12. 技术练习映射 → 面试点

* 幂等/一致性：Outbox + 幂等键；
* 高并发扣款：行锁/版本号 + 重试；
* 风控：限额/黑名单/令牌桶；
* 批 + 流：Redpanda → 聚合表 → Dashboard；
* 指标：最大回撤、年化收益、夏普（批处理模块计算）。

---

## 13. 脚手架与命令（建议）

* `make up|down|logs|migrate|seed`
* `./gradlew :backend:payment-api:bootRun`
* `./gradlew :backend:stream-worker:run`
* `npm --prefix frontend/dashboard run dev`

---

## 14. 下一步马上可做（今日待办）

1. 初始化 Monorepo 目录与 `docker-compose.yml`；
2. Flyway `V1__init.sql`（accounts/transactions/idempotency/outbox）；
3. 搭 `POST /topup`：写账本 + 幂等 + Outbox；
4. 写一个最简单的 Poller（定时任务）。

---

## 15. 附：Flyway 初始迁移模板（节选）

```sql
create table accounts (
  id uuid primary key,
  user_id uuid,
  type varchar(16) not null,
  balance_minor bigint not null default 0,
  currency char(3) not null default 'JPY',
  balance_version bigint not null default 0
);

create table transactions (
  id uuid primary key,
  user_id uuid not null,
  merchant_id uuid,
  type varchar(16) not null,
  amount_minor bigint not null,
  status varchar(16) not null,
  created_at timestamptz not null default now()
);

create table ledger_entries (
  id uuid primary key,
  txn_id uuid not null,
  posting_time timestamptz not null,
  description text
);

create table ledger_postings (
  id uuid primary key,
  entry_id uuid not null references ledger_entries(id),
  account_id uuid not null references accounts(id),
  direction varchar(8) not null,
  amount_minor bigint not null
);

create table idempotency_keys (
  key varchar(64) primary key,
  request_hash varchar(128) not null,
  response_body jsonb not null,
  status int not null,
  created_at timestamptz not null default now()
);

create table outbox_events (
  id uuid primary key,
  aggregate_type varchar(64) not null,
  aggregate_id uuid not null,
  payload jsonb not null,
  created_at timestamptz not null default now(),
  published boolean not null default false
);
```

---

## 16. 附：事件聚合 Worker（Kotlin 伪代码）

```kotlin
fun onPaymentEvent(e: PaymentEvent){
  val day = LocalDate.ofInstant(e.ts)
  repo.upsertAgg(day) { cur ->
     cur.dau.add(e.userId)
     cur.total += e.amountMinor
     cur.topMerchants[e.merchantId] += e.amountMinor
  }
}
```

---

## 17. 未来增强清单（按优先级）

* ✅ 游标分页（交易明细）；
* ✅ 幂等响应缓存；
* 🔜 Saga/TCC（提现涉及外部渠道模拟）；
* 🔜 策略回测（最大回撤/夏普/Calmar）；
* 🔜 GraphQL 聚合查询；
* 🔜 地理风控（IP→国家/城市）；
* 🔜 规则引擎（Drools 或自研 DSL）。
