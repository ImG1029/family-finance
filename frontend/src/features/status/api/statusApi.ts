import { api } from '../../../config/api'
import type { PingResponse } from '../types';

export async function pingApi(): Promise<PingResponse> {
    const response = await api.get('/ping')
    return response.data
}
