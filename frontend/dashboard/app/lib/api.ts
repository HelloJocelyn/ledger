// lib/api.ts
export const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8080";

export type ApiErrorPayload = {
    error?: string;
    message?: string;
    status?: number;
};

async function parseJsonSafe(res: Response): Promise<any> {
    const text = await res.text();
    if (!text) return null;
    try {
        return JSON.parse(text);
    } catch {
        return { raw: text };
    }
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
        const msg =
            (data && (data.error || data.message)) ||
            `HTTP ${res.status} ${res.statusText}`;
        throw new Error(msg);
    }
    return data as T;
}
