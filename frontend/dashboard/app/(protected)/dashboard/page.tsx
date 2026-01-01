"use client";

import { useEffect, useState } from "react";

export default function DashboardPage() {
  const [now, setNow] = useState<string>("");

  useEffect(() => {
    setNow(new Date().toLocaleString());
  }, []);

  return (
    <main style={styles.container}>
      <h1 style={styles.title}>📊 Dashboard</h1>

      <p style={styles.text}>
        如果你能看到这个页面，说明：
      </p>

      <ul style={styles.list}>
        <li>✅ 路由工作正常</li>
        <li>✅ 登录后跳转成功</li>
        <li>✅ App Router 没问题</li>
      </ul>

      <div style={styles.card}>
        <p style={styles.cardTitle}>当前时间</p>
        <p>{now}</p>
      </div>

      <div style={styles.card}>
        <p style={styles.cardTitle}>下一步你可以做：</p>
        <ul>
          <li>👉 加安全设置页（Passkey）</li>
          <li>👉 把这里换成真实业务数据</li>
          <li>👉 验证 post-auth / nudge 流程</li>
        </ul>
      </div>
    </main>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    padding: "40px",
    maxWidth: "800px",
    margin: "0 auto",
    fontFamily: "system-ui, -apple-system, BlinkMacSystemFont",
  },
  title: {
    fontSize: "28px",
    marginBottom: "16px",
  },
  text: {
    fontSize: "16px",
    marginBottom: "12px",
  },
  list: {
    marginBottom: "24px",
  },
  card: {
    border: "1px solid #e5e7eb",
    borderRadius: "8px",
    padding: "16px",
    marginBottom: "16px",
    background: "#fafafa",
  },
  cardTitle: {
    fontWeight: 600,
    marginBottom: "8px",
  },
};
