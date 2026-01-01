'use client';

import { useState } from 'react';
import {identifyAndSendOtp} from '@/app/lib/auth';
import { useRouter } from "next/navigation";


export default function LoginPage() {
  const router = useRouter();
  const [emailOrPhone, setEmailOrPhone] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Continue 按钮 - 邮箱/手机号登录
  const handleContinue = async () => {
    if (!emailOrPhone.trim()) {
      setError('请输入邮箱或手机号');
      return;
    }

    setLoading(true);
    setError('');

    try {
      // TODO: 实现邮箱/手机号登录逻辑
      // 这里可以发送验证码或直接验证
      const resp = await identifyAndSendOtp({
        identity: emailOrPhone.trim(),
      });
      sessionStorage.setItem(
        'otp_ctx',
        JSON.stringify(resp)
      );
      

      router.push("/verify-otp") 
    } catch (error: any) {
      setError(error?.message || '网络错误，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  // Passkey 登录
  const handlePasskeyLogin = async () => {
    setLoading(true);
    setError('');

    try {
      // Passkey 登录通常不需要用户名，但如果有输入则使用
      const username = emailOrPhone.trim() || 'user';
      const res = await loginPasskey(username);
      if (res?.verified) {
        console.log('Passkey 登录成功');
        // TODO: 跳转到成功页面或主页面
      } else {
        setError('Passkey 验证失败，请重试');
      }
    } catch (err: any) {
      setError(err?.message || 'Passkey 登录失败，请检查是否在安全环境下（HTTPS/localhost）');
    } finally {
      setLoading(false);
    }
  };

  // GitHub 登录
  const handleGitHubLogin = async () => {
    setLoading(true);
    setError('');

    try {
      // TODO: 实现 GitHub OAuth 登录
      // 可以重定向到 GitHub OAuth 授权页面
      window.location.href = '/api/auth/github';
    } catch (err: any) {
      setError(err?.message || 'GitHub 登录失败');
      setLoading(false);
    }
  };

  // 钥匙图标 SVG (带向右箭头)
  const KeyIcon = () => (
    <svg
      width="16"
      height="16"
      viewBox="0 0 16 16"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      style={{ marginRight: '8px' }}
    >
      {/* 钥匙头部圆环 */}
      <path
        d="M5 7C5 5.89543 5.89543 5 7 5C8.10457 5 9 5.89543 9 7C9 8.10457 8.10457 9 7 9C5.89543 9 5 8.10457 5 7Z"
        fill="currentColor"
      />
      {/* 钥匙柄 */}
      <path
        d="M9 7H12M12 7V9M12 9H13.5"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      {/* 向右箭头 */}
      <path
        d="M13.5 7L15 8.5L13.5 10"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );

  // GitHub 图标 SVG
  const GitHubIcon = () => (
    <svg
      width="16"
      height="16"
      viewBox="0 0 16 16"
      fill="currentColor"
      style={{ marginRight: '8px' }}
    >
      <path
        fillRule="evenodd"
        clipRule="evenodd"
        d="M8 0C3.58 0 0 3.58 0 8C0 11.54 2.29 14.53 5.47 15.59C5.87 15.66 6.02 15.42 6.02 15.21C6.02 15.02 6.01 14.39 6.01 13.72C4 14.09 3.48 13.23 3.32 12.78C3.23 12.55 2.84 11.84 2.5 11.65C2.22 11.5 1.82 11.13 2.49 11.12C3.12 11.11 3.57 11.7 3.72 11.94C4.44 13.15 5.59 12.81 6.05 12.6C6.12 12.08 6.33 11.73 6.56 11.53C4.78 11.33 2.92 10.64 2.92 7.58C2.92 6.71 3.23 5.99 3.74 5.43C3.66 5.23 3.38 4.41 3.82 3.31C3.82 3.31 4.49 3.1 6.02 4.13C6.66 3.95 7.34 3.86 8.02 3.86C8.7 3.86 9.38 3.95 10.02 4.13C11.55 3.09 12.22 3.31 12.22 3.31C12.66 4.41 12.38 5.23 12.3 5.43C12.81 5.99 13.12 6.7 13.12 7.58C13.12 10.65 11.25 11.33 9.47 11.53C9.76 11.78 10.01 12.26 10.01 13.01C10.01 14.08 10 14.94 10 15.21C10 15.42 10.15 15.67 10.55 15.59C13.71 14.53 16 11.53 16 8C16 3.58 12.42 0 8 0Z"
      />
    </svg>
  );

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
          Sign in to LedgerX
        </h1>

        {/* 登录卡片 */}
        <div
          style={{
            backgroundColor: 'white',
            borderRadius: '0.5rem',
            padding: '1.5rem',
            boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          }}
        >
          {/* Email/Phone 输入框 */}
          <div style={{ marginBottom: '1rem' }}>
            <label
              htmlFor="email-phone"
              style={{
                display: 'block',
                fontSize: '0.875rem',
                fontWeight: '500',
                color: '#374151',
                marginBottom: '0.5rem',
              }}
            >
              Email or phone number
            </label>
            <input
              id="email-phone"
              type="text"
              value={emailOrPhone}
              onChange={(e) => {
                setEmailOrPhone(e.target.value);
                setError('');
              }}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !loading) {
                  handleContinue();
                }
              }}
              placeholder="Email or phone number"
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
          {error && (
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
                {error}
              </p>
            </div>
          )}

          {/* Continue 按钮 */}
          <button
            onClick={handleContinue}
            disabled={loading}
            style={{
              width: '100%',
              backgroundColor: loading ? '#9ca3af' : '#2563eb',
              color: 'white',
              fontWeight: '500',
              padding: '0.75rem',
              borderRadius: '0.375rem',
              border: 'none',
              cursor: loading ? 'not-allowed' : 'pointer',
              fontSize: '0.875rem',
              marginBottom: '1.5rem',
            }}
          >
            Continue
          </button>

          {/* 分隔线 */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              marginBottom: '1.5rem',
            }}
          >
            <div
              style={{
                flex: 1,
                height: '1px',
                backgroundColor: '#e5e7eb',
              }}
            />
            <span
              style={{
                padding: '0 1rem',
                fontSize: '0.875rem',
                color: '#6b7280',
              }}
            >
              or
            </span>
            <div
              style={{
                flex: 1,
                height: '1px',
                backgroundColor: '#e5e7eb',
              }}
            />
          </div>

          {/* Passkey 按钮 */}
          <button
            onClick={handlePasskeyLogin}
            disabled={loading}
            style={{
              width: '100%',
              backgroundColor: 'white',
              color: '#374151',
              fontWeight: '500',
              padding: '0.75rem',
              borderRadius: '0.375rem',
              border: '1px solid #d1d5db',
              cursor: loading ? 'not-allowed' : 'pointer',
              fontSize: '0.875rem',
              marginBottom: '0.75rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <KeyIcon />
            Continue with Passkey
          </button>

          {/* GitHub 按钮 */}
          <button
            onClick={handleGitHubLogin}
            disabled={loading}
            style={{
              width: '100%',
              backgroundColor: 'white',
              color: '#374151',
              fontWeight: '500',
              padding: '0.75rem',
              borderRadius: '0.375rem',
              border: '1px solid #d1d5db',
              cursor: loading ? 'not-allowed' : 'pointer',
              fontSize: '0.875rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <GitHubIcon />
            Continue with GitHub
          </button>
        </div>

        {/* 底部链接 */}
        <div style={{ marginTop: '1.5rem', textAlign: 'center' }}>
          <p
            style={{
              fontSize: '0.875rem',
              color: '#6b7280',
              marginBottom: '0.75rem',
            }}
          >
            By continuing, you agree to{' '}
            <a
              href="/terms"
              style={{
                color: '#2563eb',
                textDecoration: 'underline',
              }}
            >
              Terms & Privacy.
            </a>
          </p>
          <a
            href="/trouble-signing-in"
            style={{
              color: '#2563eb',
              textDecoration: 'underline',
              fontSize: '0.875rem',
            }}
          >
            Trouble signing in?
          </a>
        </div>
      </div>
    </div>
  );
}
