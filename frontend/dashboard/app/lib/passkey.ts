
  export async function isWebAuthnSupported(): Promise<boolean> {
    if (
      typeof window === "undefined" ||
      !window.PublicKeyCredential ||
      typeof window.PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable !== "function"
    ) {
      return false;
    }
  
    try {
      return await window.PublicKeyCredential
        .isUserVerifyingPlatformAuthenticatorAvailable();
    } catch {
      return false;
    }
  }

  export async function isPlatformAuthenticatorAvailableSafe() {
    try {
      if (!isWebAuthnSupported()) return false;
      if (!PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable) return true;
      return await PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable();
    } catch {
      return false;
    }
  }