'use client';

import { useState } from 'react';

type LoginStep = 'email' | 'verification' | 'success';

export default function LoginPage() {
  const [step, setStep] = useState<LoginStep>('email');
  const [email, setEmail] = useState('');
  const [verificationCode, setVerificationCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [countdown, setCountdown] = useState(0);

  // 发送验证码
  const sendVerificationCode = async () => {
    if (!email) {
      setError('请输入邮箱地址');
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setError('请输入有效的邮箱地址');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await fetch('/api/auth/send-code', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email }),
      });

      const data = await response.json();

      if (response.ok) {
        setStep('verification');
        startCountdown();
      } else {
        setError(data.message || '发送验证码失败');
      }
    } catch (error) {
      setError('网络错误，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  // 验证登录
  const verifyLogin = async () => {
    if (!verificationCode) {
      setError('请输入验证码');
      return;
    }

    if (verificationCode.length !== 6) {
      setError('验证码应为6位数字');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await fetch('/api/auth/verify', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          email, 
          code: verificationCode 
        }),
      });

      const data = await response.json();

      if (response.ok) {
        setStep('success');
      } else {
        setError(data.message || '验证码错误或已过期');
      }
    } catch (error) {
      setError('网络错误，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  // 倒计时功能
  const startCountdown = () => {
    setCountdown(60);
    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  // 重新发送验证码
  const resendCode = async () => {
    if (countdown > 0) return;
    await sendVerificationCode();
  };

  return (
    <div style={{ 
      minHeight: '100vh', 
      background: 'linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%)', 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'center', 
      padding: '1rem' 
    }}>
      <div style={{ maxWidth: '28rem', width: '100%' }}>
        {/* 头部标题 */}
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <h1 style={{ 
            fontSize: '2.25rem', 
            fontWeight: 'bold', 
            color: '#111827', 
            marginBottom: '0.5rem' 
          }}>
            LedgerX
          </h1>
          <p style={{ color: '#6b7280' }}>使用邮箱验证码登录</p>
        </div>

        {/* 登录卡片 */}
        <div style={{ 
          backgroundColor: 'white', 
          borderRadius: '1rem', 
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)', 
          padding: '2rem' 
        }}>
          {step === 'email' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              <div>
                <label htmlFor="email" style={{ 
                  display: 'block', 
                  fontSize: '0.875rem', 
                  fontWeight: '600', 
                  color: '#374151', 
                  marginBottom: '0.5rem' 
                }}>
                  邮箱地址
                </label>
                <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  style={{ 
                    width: '100%', 
                    padding: '0.75rem 1rem', 
                    border: '1px solid #d1d5db', 
                    borderRadius: '0.5rem', 
                    outline: 'none',
                    fontSize: '1rem'
                  }}
                  placeholder="请输入您的邮箱地址"
                />
              </div>

              {error && (
                <div style={{ 
                  backgroundColor: '#fef2f2', 
                  border: '1px solid #fecaca', 
                  borderRadius: '0.5rem', 
                  padding: '0.75rem' 
                }}>
                  <p style={{ color: '#dc2626', fontSize: '0.875rem' }}>{error}</p>
                </div>
              )}

              <button
                onClick={sendVerificationCode}
                disabled={loading}
                style={{
                  width: '100%',
                  backgroundColor: loading ? '#9ca3af' : '#2563eb',
                  color: 'white',
                  fontWeight: '600',
                  padding: '0.75rem 1rem',
                  borderRadius: '0.5rem',
                  border: 'none',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  fontSize: '1rem'
                }}
              >
                {loading ? '发送中...' : '发送验证码'}
              </button>
            </div>
          )}

          {step === 'verification' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              <div>
                <label htmlFor="code" style={{ 
                  display: 'block', 
                  fontSize: '0.875rem', 
                  fontWeight: '600', 
                  color: '#374151', 
                  marginBottom: '0.5rem' 
                }}>
                  验证码
                </label>
                <input
                  id="code"
                  name="code"
                  type="text"
                  required
                  value={verificationCode}
                  onChange={(e) => setVerificationCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                  style={{ 
                    width: '100%', 
                    padding: '0.75rem 1rem', 
                    border: '1px solid #d1d5db', 
                    borderRadius: '0.5rem', 
                    outline: 'none',
                    fontSize: '1.25rem',
                    textAlign: 'center',
                    letterSpacing: '0.1em',
                    fontFamily: 'monospace'
                  }}
                  placeholder="请输入6位验证码"
                />
                <p style={{ 
                  marginTop: '0.5rem', 
                  fontSize: '0.875rem', 
                  color: '#6b7280', 
                  textAlign: 'center' 
                }}>
                  验证码已发送至 <span style={{ fontWeight: '500' }}>{email}</span>
                </p>
              </div>

              {error && (
                <div style={{ 
                  backgroundColor: '#fef2f2', 
                  border: '1px solid #fecaca', 
                  borderRadius: '0.5rem', 
                  padding: '0.75rem' 
                }}>
                  <p style={{ color: '#dc2626', fontSize: '0.875rem' }}>{error}</p>
                </div>
              )}

              <button
                onClick={verifyLogin}
                disabled={loading || verificationCode.length !== 6}
                style={{
                  width: '100%',
                  backgroundColor: (loading || verificationCode.length !== 6) ? '#9ca3af' : '#2563eb',
                  color: 'white',
                  fontWeight: '600',
                  padding: '0.75rem 1rem',
                  borderRadius: '0.5rem',
                  border: 'none',
                  cursor: (loading || verificationCode.length !== 6) ? 'not-allowed' : 'pointer',
                  fontSize: '1rem'
                }}
              >
                {loading ? '验证中...' : '验证登录'}
              </button>

              <div style={{ 
                textAlign: 'center', 
                display: 'flex', 
                flexDirection: 'column', 
                gap: '0.75rem' 
              }}>
                <button
                  onClick={resendCode}
                  disabled={countdown > 0}
                  style={{
                    color: countdown > 0 ? '#9ca3af' : '#2563eb',
                    cursor: countdown > 0 ? 'not-allowed' : 'pointer',
                    fontSize: '0.875rem',
                    fontWeight: '500',
                    background: 'none',
                    border: 'none'
                  }}
                >
                  {countdown > 0 ? `${countdown}秒后重新发送` : '重新发送验证码'}
                </button>
                
                <div>
                  <button
                    onClick={() => {
                      setStep('email');
                      setVerificationCode('');
                      setError('');
                      setCountdown(0);
                    }}
                    style={{
                      color: '#6b7280',
                      fontSize: '0.875rem',
                      background: 'none',
                      border: 'none',
                      cursor: 'pointer'
                    }}
                  >
                    返回修改邮箱
                  </button>
                </div>
              </div>
            </div>
          )}

          {step === 'success' && (
            <div style={{ 
              textAlign: 'center', 
              display: 'flex', 
              flexDirection: 'column', 
              gap: '1.5rem' 
            }}>
              <div style={{ 
                margin: '0 auto', 
                width: '4rem', 
                height: '4rem', 
                backgroundColor: '#dcfce7', 
                borderRadius: '50%', 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center' 
              }}>
                <svg
                  style={{ width: '2rem', height: '2rem', color: '#16a34a' }}
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              </div>
              
              <div>
                <h2 style={{ 
                  fontSize: '1.5rem', 
                  fontWeight: 'bold', 
                  color: '#111827', 
                  marginBottom: '0.5rem' 
                }}>
                  登录成功！
                </h2>
                <p style={{ color: '#6b7280' }}>
                  欢迎回来，<span style={{ fontWeight: '500', color: '#2563eb' }}>{email}</span>
                </p>
              </div>
              
              <button
                onClick={() => {
                  setStep('email');
                  setEmail('');
                  setVerificationCode('');
                  setError('');
                  setCountdown(0);
                }}
                style={{
                  width: '100%',
                  backgroundColor: '#4b5563',
                  color: 'white',
                  fontWeight: '600',
                  padding: '0.75rem 1rem',
                  borderRadius: '0.5rem',
                  border: 'none',
                  cursor: 'pointer',
                  fontSize: '1rem'
                }}
              >
                重新登录
              </button>
            </div>
          )}
        </div>

        {/* 底部信息 */}
        <div style={{ textAlign: 'center', marginTop: '2rem' }}>
          <p style={{ fontSize: '0.875rem', color: '#6b7280' }}>
            登录即表示您同意我们的服务条款和隐私政策
          </p>
        </div>
      </div>
    </div>
  );
}