# 📊 Market Insight Engine Plan

## 🎯 项目目标

构建一个“市场洞察引擎（Market Insight Engine）”，用于：

- 结构化市场数据
- 验证个人市场判断
- 识别趋势 / 情绪 / 博弈阶段
- 辅助投资决策（非自动交易）

### 模块分层

1. 数据层（Market Data）
2. 计算层（Signal Engine）
3. 认知层（Insight Layer）
4. 展示层（Dashboard / API）

---

# 🚀 Phase 1：MVP（1-2周）

## 🎯 目标

- 建立基础数据流
- 实现简单趋势判断
- 跑通完整流程

---

## 📦 功能

### 1️⃣ 行情数据

**表：stock_price**

| 字段     | 类型     | 说明   |
|--------|--------|------|
| symbol | string | 股票代码 |
| date   | date   | 日期   |
| open   | double | 开盘价  |
| close  | double | 收盘价  |
| high   | double | 最高价  |
| low    | double | 最低价  |
| volume | double | 成交量  |

---

### 2️⃣ 指数数据

**表：index_price**

| 字段         | 类型     | 说明 |
|------------|--------|----|
| index_code | string | 指数 |
| date       | date   | 日期 |
| close      | double | 收盘 |

---

### 3️⃣ 指标计算

- 5日涨幅
- 14日涨幅
- 成交量变化

---

### 4️⃣ 相对强度

relative_strength = stock_return - index_return


---

### 5️⃣ 信号规则（基础版）

IF:

14天涨幅 > 8%
相对强度 > 5%

THEN:
→ 标记为 强势股


---

### 6️⃣ 定时任务

```java
@Scheduled(cron = "0 0 18 * * ?")
```

任务内容：

拉取行情数据
计算指标
存储信号

# Phase 2：市场认知模型（核心）

🎯 目标

将“市场感觉”结构化为可验证模型

🧩 市场阶段分类
1️⃣ 趋势（Trend）

特征：

连续上涨 ≥ 5天
回撤小
成交量稳定

代表：

机构资金驱动
2️⃣ 情绪（Sentiment）

特征：

短期暴涨（3天 > 10%）
波动大
回撤明显

代表：

热点 / 散户推动
3️⃣ 博弈（Game Phase）

特征：

无明显方向
上下震荡
波动率高

代表：

多空对抗
📦 表：market_signal
字段 类型 说明
symbol string 股票
date date 日期
signal_type enum TREND / SENTIMENT / GAME
score double 强度评分
reason string 触发原因

# Phase 3：信号验证系统

🎯 目标

验证策略是否有效

📊 验证逻辑

示例：

某股票被标记为 TREND
7天后涨跌情况如何？
📦 表：signal_performance
字段 类型 说明
signal_id id 信号ID
future_return_7d double 7天收益
future_return_14d double 14天收益
🧠 输出能力
哪种信号最有效？
哪种市场阶段更容易赚钱？

# Phase 4：板块轮动分析

🎯 目标

识别市场热点与资金流向

📊 功能
板块平均涨幅
板块上涨股票比例
板块持续性
📦 表：sector_strength
字段 类型 说明
sector string 板块
date date 日期
avg_return_14d double 平均涨幅
rising_stock_ratio double 上涨比例
🧠 输出能力
当前最强板块 Top N
板块是否持续上涨
判断趋势 vs 情绪

# Phase 5：展示层（可选）

页面建议
1️⃣ 强势股榜单
按相对强度排序
2️⃣ 板块排名
Top 热门板块
3️⃣ 信号列表
今日触发信号

# 设计原则

✅ 做
用数据验证认知
资金状态识别模型（轮动 / 流入 / 收缩）
保持模型简单
快速迭代
❌ 不做
不追求预测市场
不做复杂模型
不一开始做自动交易

indicators
breadth
sector rotation
signal engine