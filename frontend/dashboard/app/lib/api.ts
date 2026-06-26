// lib/api.ts
export const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8080";
export const MARKET_API_BASE = process.env.NEXT_PUBLIC_MARKET_API_BASE ?? "http://localhost:8081";
export const ANALYSIS_API_BASE = process.env.NEXT_PUBLIC_ANALYSIS_API_BASE ?? "http://localhost:8082";

export type ApiErrorPayload = {
    error?: string;
    message?: string;
    status?: number;
};

async function parseJsonSafe(res: Response): Promise<unknown> {
    const text = await res.text();
    if (!text) return null;
    try {
        return JSON.parse(text);
    } catch {
        return { raw: text };
    }
}

function apiErrorMessage(data: unknown, fallback: string): string {
    if (data && typeof data === "object") {
        const payload = data as ApiErrorPayload;
        return payload.error || payload.message || fallback;
    }
    return fallback;
}

export async function apiGet<T>(path: string, baseUrl: string = API_BASE): Promise<T> {
    const token = sessionStorage.getItem("signup_token");
    const headers: HeadersInit = {};
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }
    const res = await fetch(`${baseUrl}${path}`, {
        method: "GET",
        headers,
        credentials: "include",
    });

    const data = await parseJsonSafe(res);

    if (!res.ok) {
        if (res.status === 401) {
            sessionStorage.removeItem("signup_token");
        }
        const msg = apiErrorMessage(data, `HTTP ${res.status} ${res.statusText}`);
        throw new Error(msg);
    }
    return data as T;
}

export async function apiPost<T>(path: string, body: unknown): Promise<T> {
    const token = sessionStorage.getItem("signup_token");
    const headers:HeadersInit = {
        "Content-Type": "application/json"
    }
    if(token){
        headers["Authorization"] = `Bearer ${token}`;
    }
    const res = await fetch(`${API_BASE}${path}`, {
        method: "POST",
        headers,
        credentials: "include",
        body: JSON.stringify(body),
    });
   

    const data = await parseJsonSafe(res);

    if (!res.ok) {
        if(res.status === 401){
            sessionStorage.removeItem("signup_token");
        }
        const msg = apiErrorMessage(data, `HTTP ${res.status} ${res.statusText}`);
        throw new Error(msg);
    }
    return data as T;
}
