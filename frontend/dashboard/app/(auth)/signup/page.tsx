'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { createAccount } from '../../lib/auth';

function readSignupToken(): string | null {
  try {
    return sessionStorage.getItem('signup_token');
  } catch {
    return null;
  }
}

export default function CreateAccountPage() {
  const router = useRouter();
  const [signupToken, setSignupToken] = useState<string | null>(null);
  const [displayName, setDisplayName] = useState('');
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState<string | null>(null);

  useEffect(() => {
    const t = readSignupToken();
    if (!t) {
      router.replace('/');
      return;
    }
    setSignupToken(t);
  }, [router]);

  async function onSubmit() {
    if (!signupToken) return;

    const name = displayName.trim();
    if (name.length < 2) {
      setMsg('Display name must be at least 2 characters.');
      return;
    }

    setBusy(true);
    setMsg(null);
    try {
      const r = await createAccount({ signupToken, displayName: name });

      // 清理
      sessionStorage.removeItem('signup_token');

      if (r.ok && r.next === 'SIGNED_IN') {
        router.replace('/'); // or /dashboard
      } else {
        setMsg('Unexpected response.');
      }
    } catch (e: any) {
      setMsg(e?.message ?? String(e));
    } finally {
      setBusy(false);
    }
  }

  if (!signupToken) return null;

  return (
    <div
      style={{
        minHeight: '100vh',
        background: '#f5f5f5',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '1rem',
      }}
    >
      <div style={{ maxWidth: '400px', width: '100%' }}>
        {/* 标题 */}
        <h1
          style={{
            fontSize: '1.875rem',
            fontWeight: '600',
            color: '#374151',
            marginBottom: '2rem',
            textAlign: 'center',
          }}
        >
          Create your LedgerX account
        </h1>

        {/* 创建账户卡片 */}
        <div
          style={{
            backgroundColor: 'white',
            borderRadius: '0.5rem',
            padding: '1.5rem',
            boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          }}
        >
          {/* 提示信息 */}
          <p
            style={{
              fontSize: '0.875rem',
              color: '#6b7280',
              marginBottom: '1.5rem',
              textAlign: 'center',
            }}
          >
            Choose a display name. You can change it later.
          </p>

          {/* Display name 输入框 */}
          <div style={{ marginBottom: '1rem' }}>
            <label
              htmlFor="display-name"
              style={{
                display: 'block',
                fontSize: '0.875rem',
                fontWeight: '500',
                color: '#374151',
                marginBottom: '0.5rem',
              }}
            >
              Display name
            </label>
            <input
              id="display-name"
              type="text"
              value={displayName}
              onChange={(e) => {
                setDisplayName(e.target.value);
                setMsg(null);
              }}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !busy && displayName.trim().length >= 2) {
                  onSubmit();
                }
              }}
              placeholder="e.g. Jocelyn"
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                outline: 'none',
                fontSize: '0.875rem',
                boxSizing: 'border-box',
              }}
            />
          </div>

          {/* 错误提示 */}
          {msg && (
            <div
              style={{
                backgroundColor: '#fef2f2',
                border: '1px solid #fecaca',
                borderRadius: '0.375rem',
                padding: '0.75rem',
                marginBottom: '1rem',
              }}
            >
              <p style={{ color: '#dc2626', fontSize: '0.875rem', margin: 0 }}>
                {msg}
              </p>
            </div>
          )}

          {/* Create account 按钮 */}
          <button
            onClick={onSubmit}
            disabled={busy || displayName.trim().length < 2}
            style={{
              width: '100%',
              backgroundColor: busy || displayName.trim().length < 2 ? '#9ca3af' : '#2563eb',
              color: 'white',
              fontWeight: '500',
              padding: '0.75rem',
              borderRadius: '0.375rem',
              border: 'none',
              cursor: busy || displayName.trim().length < 2 ? 'not-allowed' : 'pointer',
              fontSize: '0.875rem',
            }}
          >
            {busy ? 'Creating...' : 'Create account'}
          </button>
        </div>

        {/* 底部链接 */}
        <div style={{ marginTop: '1.5rem', textAlign: 'center' }}>
          <a
            href="/"
            style={{
              color: '#2563eb',
              textDecoration: 'underline',
              fontSize: '0.875rem',
            }}
          >
            Back to sign in
          </a>
        </div>
      </div>
    </div>
  );
}
