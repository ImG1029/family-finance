export interface HelloResponse {
    message: string,
    timestamp: string;
}

const API_URL = import.meta.env.VITE_API_URL

export async function fetchHello(): Promise<HelloResponse> {
    const response = await fetch(`${API_URL}/ping`)
    console.log("TEST")
    console.log(API_URL)
    if (!response.ok) {
        throw new Error(`Erro HTTP: ${response.status}`);
    }
    return response.json()
}
