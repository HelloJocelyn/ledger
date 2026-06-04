# LedgerX AI功能开发计划

## 目标

将LedgerX升级为AI驱动的个人财务助手，核心功能：
1. **账单图片识别** — 拍照上传，自动识别入库
2. **消费模式分析 + 建议** — RAG + 结构化数据结合
3. **股市新闻摘要 + 持仓关联** — 自动关联持仓相关新闻

---

## 整体架构

```
[前端 (上传图片 / 查询)] 
        ↓
[Spring Boot (Java)] 
        ↓
[Python AI Service]  ←→  [Claude API]
        ↓
[MySQL]         [ChromaDB (向量数据库)]
```

**设计原则：**
- Spring Boot保持不动，负责业务逻辑和数据管理
- 新增Python微服务专门处理AI逻辑，两者通过REST API通信
- AI生态在Python里，不在Java里硬做

---

## 功能一：账单图片识别入库

### 流程

```
手机截图 → 前端上传 → Spring Boot → Python AI服务
→ Claude Vision识别 → 结构化JSON → MySQL入库
```

### Claude Vision识别输出格式

```json
{
  "date": "2025-06-01",
  "merchant": "スターバックス",
  "amount": 650,
  "currency": "JPY",
  "payment_method": "PayPay",
  "category": "カフェ",
  "raw_text": "..."
}
```

### MySQL表设计

```sql
CREATE TABLE transactions (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  date        DATE NOT NULL,
  merchant    VARCHAR(255),
  amount      DECIMAL(10,2) NOT NULL,
  currency    VARCHAR(10) DEFAULT 'JPY',
  category    VARCHAR(100),
  payment_method VARCHAR(100),
  raw_text    TEXT,
  image_path  VARCHAR(500),
  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Action

- [ ] Python环境搭建，跑通Claude Vision API最小demo
- [ ] 设计识别Prompt，确保返回稳定的JSON格式
- [ ] Spring Boot新增 `/api/transactions/upload` 接口，接收图片转发给Python服务
- [ ] Python服务实现图片→base64→Claude Vision→JSON解析
- [ ] 识别结果写回MySQL
- [ ] 前端新增上传页面

---

## 功能二：消费模式分析 + 建议

### 流程

```
用户提问（自然语言）→ Spring Boot → Python AI服务
→ ChromaDB检索相关账单 → 喂给Claude → 返回分析建议
```

### 支持的查询示例

- "上个月餐饮花了多少？"
- "我最近三个月的消费趋势是什么？"
- "哪个类别超支了？给我一些建议。"

### RAG设计

```python
# 账单向量化存入ChromaDB
def vectorize_transactions():
    transactions = fetch_from_mysql()
    for t in transactions:
        text = f"{t['date']} {t['merchant']} {t['category']} {t['amount']}円"
        collection.add(documents=[text], ids=[str(t['id'])])

# 查询时检索相关账单
def query_transactions(user_question):
    results = collection.query(query_texts=[user_question], n_results=20)
    context = "\n".join(results['documents'][0])
    return ask_claude(user_question, context)
```

### 月次レポート（定期生成）

- Spring Boot定时任务，每月1日触发
- 调Python服务生成上月消费报告 + 改善建议
- 存入MySQL，前端展示

### Action

- [ ] 安装ChromaDB，搭建向量化pipeline
- [ ] 把现有MySQL账单数据向量化入库
- [ ] 实现自然语言查询接口
- [ ] 设计分析Prompt（消费建议、超支预警等）
- [ ] Spring Boot定时任务触发月次レポート生成
- [ ] 前端新增分析页面 + 对话查询入口

---

## 功能三：股市新闻摘要 + 持仓关联

### 流程

```
Yahoo Finance API拉取新闻 → Claude摘要
→ 关联MySQL持仓数据 → 生成"今日持仓相关新闻"
```

### MySQL持仓表设计

```sql
CREATE TABLE holdings (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  ticker      VARCHAR(20) NOT NULL,
  name        VARCHAR(255),
  quantity    DECIMAL(10,4),
  cost_price  DECIMAL(10,2),
  currency    VARCHAR(10) DEFAULT 'JPY',
  updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 新闻摘要输出格式

```json
{
  "date": "2025-06-04",
  "ticker": "7203",
  "company": "トヨタ自動車",
  "headline": "...",
  "summary": "...",
  "impact": "positive / negative / neutral",
  "relevance": "持仓関連: 保有100株"
}
```

### Action

- [ ] 安装yfinance，测试日本株式データ取得
- [ ] 实现新闻拉取 + Claude摘要pipeline
- [ ] 持仓数据关联逻辑
- [ ] 定时任务（毎朝8時）生成今日新闻摘要
- [ ] 前端新增持仓 + 新闻页面

---

## 开发顺序建议

| 阶段 | 内容 | 预估时间 |
|------|------|----------|
| Phase 1 | 环境搭建 + Claude API跑通 | 1天 |
| Phase 2 | 账单图片识别入库 | 1-2周 |
| Phase 3 | 消费模式分析 + RAG | 2-3周 |
| Phase 4 | 股市新闻 + 持仓关联 | 1-2周 |

---

## Phase 1 详细步骤（马上可以做）

```bash
# 1. 建Python虚拟环境
python -m venv venv
source venv/bin/activate  # Mac/Linux

# 2. 安装依赖
pip install anthropic langchain chromadb yfinance fastapi uvicorn
```

```python
# 3. 跑通最小demo — 账单图片识别
import anthropic
import base64

client = anthropic.Anthropic()

with open("receipt.jpg", "rb") as f:
    image_data = base64.standard_b64encode(f.read()).decode("utf-8")

message = client.messages.create(
    model="claude-opus-4-20250514",
    max_tokens=1024,
    messages=[{
        "role": "user",
        "content": [
            {
                "type": "image",
                "source": {
                    "type": "base64",
                    "media_type": "image/jpeg",
                    "data": image_data,
                },
            },
            {
                "type": "text",
                "text": """以下のレシート画像から情報を抽出し、JSONのみ返してください。
{
  "date": "YYYY-MM-DD",
  "merchant": "店名",
  "amount": 金額(数字のみ),
  "currency": "JPY",
  "payment_method": "支払方法",
  "category": "カテゴリ"
}"""
            }
        ],
    }]
)

print(message.content[0].text)
```

---

## 技術スタック まとめ

| レイヤー | 技術 |
|------|------|
| 既存バックエンド | Java, Spring Boot, MySQL |
| AI Service | Python, FastAPI |
| AI Model | Claude API (Anthropic) |
| Vector DB | ChromaDB |
| 株式データ | yfinance |
| コンテナ | Docker |