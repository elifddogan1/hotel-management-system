import api from './api';

// Backend'deki PagedResponse yapısı
export interface PagedResponse<T> {
    content: T[];
    pageNo: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

// Backend'deki RoomResponse.QueryDetail DTO'su
export interface RoomQueryDetail {
    id: number;
    roomNumber: string;
    roomType: string;
    maxCapacity: number;
    hotelId: number;
    hotelName: string;
}

// Backend'deki RoomResponse.Creation DTO'su
export interface RoomCreationResponse {
    id: number;
    roomNumber: string;
    roomType: string;
    maxCapacity: number;
    hotelId: number;
}

// Backend'deki RoomRequest.Creation DTO'su
export interface RoomCreationRequest {
    hotelId: number; // Artık zorunlu
    roomNumber: string;
    roomType: string;
    maxCapacity: number;
}

// Sayfalama ve filtreleme için RoomRequest.Search DTO'su
export interface RoomSearchParams {
    roomNumber?: string;
    roomType?: string;
    hotelId?: number;
    page?: number;
    size?: number;
    sortBy?: string;
    direction?: string;
}

export interface AvailableRoomSearchParams {
    hotelId?: number;
    numberOfPerson: number;
    checkInDate: string; // 'YYYY-MM-DD' formatında gönderilmelidir
    checkOutDate: string; // 'YYYY-MM-DD' formatında gönderilmelidir
}

export const roomService = {
    // Sayfalamalı ve filtrelemeli ana arama
    getRooms: async (params?: RoomSearchParams): Promise<PagedResponse<RoomQueryDetail>> => {
        const response = await api.get<PagedResponse<RoomQueryDetail>>('/rooms', { params });
        return response.data;
    },

    // Sayfalama olmadan tüm odaları getirme
    getAllRooms: async (): Promise<RoomQueryDetail[]> => {
        const response = await api.get<RoomQueryDetail[]>('/rooms/all');
        return response.data;
    },

    // Hotel ID'sine göre odaları getirme
    getRoomsByHotelId: async (hotelId: number): Promise<RoomQueryDetail[]> => {
        const response = await api.get<RoomQueryDetail[]>(`/rooms/hotel/${hotelId}`);
        return response.data;
    },

    // ID'ye göre tekil oda detayını getirme
    getRoomById: async (roomId: number): Promise<RoomQueryDetail> => {
        const response = await api.get<RoomQueryDetail>(`/rooms/${roomId}`);
        return response.data;
    },

    // Müsaitlik durumuna göre oda arama
    getAvailableRooms: async (params: AvailableRoomSearchParams): Promise<RoomQueryDetail[]> => {
        if (params.hotelId) {
            // Belirli bir otele göre arama (/available)
            const response = await api.get<RoomQueryDetail[]>('/rooms/available', {
                params: {
                    hotelId: params.hotelId,
                    numberOfPerson: params.numberOfPerson,
                    checkInDate: params.checkInDate,
                    checkOutDate: params.checkOutDate
                }
            });
            return response.data;
        } else {
            // Tüm otellerde arama (/available-all)
            const response = await api.get<RoomQueryDetail[]>('/rooms/available-all', {
                params: {
                    numberOfPerson: params.numberOfPerson,
                    checkInDate: params.checkInDate,
                    checkOutDate: params.checkOutDate
                }
            });
            return response.data;
        }
    },

    // Yeni oda oluşturma (Path'ten hotelId çıkarıldı, POST /rooms/create yapısına uygun hale getirildi)
    createRoom: async (room: RoomCreationRequest): Promise<RoomCreationResponse> => {
        const response = await api.post<RoomCreationResponse>('/rooms/create', room);
        return response.data;
    },

    // Oda güncelleme
    updateRoom: async (roomId: number, room: RoomCreationRequest): Promise<RoomCreationResponse> => {
        const response = await api.put<RoomCreationResponse>(`/rooms/${roomId}`, room);
        return response.data;
    },

    // Oda silme
    deleteRoom: async (roomId: number): Promise<void> => {
        await api.delete(`/rooms/${roomId}`);
    }
};