import { API_BASE, apiPost } from './api';
export type OtpCtx = {
    identityType: "EMAIL" | "PHONE";
    normalizedIdentity: string;
    maskedIdentity: string;
    expiresInSeconds: number;
    cooldownSeconds: number;
};

export type VerifyOtpResp =
  | {
      verified: false;
      errorCode: string;
    }
  | {
      verified: true;
      next: "SIGNED_IN";
      signupToken: string;
      hasPasskey: false;
    }
  | {
      verified: true;
      next: "CREATE_ACCOUNT";
      signupToken: string;
      hasPasskey: false;
    };


export type CreateAccountResp = {
    ok: boolean,
    next: "SIGNED_IN" | "CREATE_ACCOUNT";
    userUuid: string;
};

export async function identifyAndSendOtp(payload: {
    identity: string;
}): Promise<OtpCtx> {
    return await apiPost<OtpCtx>("/api/auth/identify-and-send-otp", payload);
}

export async function verifyOtp(payload: {
    identityType: "EMAIL" | "PHONE";
    identity: string;
    code: string;
}): Promise<VerifyOtpResp> {
    return await apiPost<VerifyOtpResp>("/api/auth/otp/verify", payload);
}

export async function resendOtp(payload: {
    identityType: "EMAIL" | "PHONE";
    identity: string;
}): Promise<OtpCtx> {
    return await apiPost<OtpCtx>("/api/auth/otp/resend-otp", payload);
}

export async function createAccount(payload: { signupToken: string; displayName: string }) {
    
  
    return await apiPost<CreateAccountResp>("/api/auth/create-account",payload)
    
  }