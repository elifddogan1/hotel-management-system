import api from './api';

// 1. Veri Yapısını Güvenceye Almak İçin Interface Tanımı
export interface Hotel {
    id?: number; // Ekleme yaparken id olmayacağı için opsiyonel (?) yaptık
    name: string;
    location: string;
    contactInfo: string;
}

// 2. API İstek Fonksiyonları
export const hotelService = {
    // Tüm otelleri listele (GET)
    getAllHotels: async (): Promise<Hotel[]> => {
        const response = await api.get<Hotel[]>('/hotels');
        return response.data;
    },

    getHotelById: async (id: number): Promise<Hotel> => {
        const response = await api.get<Hotel>(`/hotels/${id}`);
        return response.data;
    },

    // Yeni otel ekle (POST)
    createHotel: async (hotel: Hotel): Promise<Hotel> => {
        const response = await api.post<Hotel>('/hotels', hotel);
        return response.data;
    },

    // Otel sil (DELETE)
    deleteHotel: async (id: number): Promise<void> => {
        await api.delete(`/hotels/${id}`);
    },

    // Otel güncelle (PUT)
    updateHotel: async (id: number, hotel: Hotel): Promise<Hotel> => {
        const response = await api.put<Hotel>(`/hotels/${id}`, hotel);
        return response.data;
    }
}