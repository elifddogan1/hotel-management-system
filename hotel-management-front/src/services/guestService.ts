import api from './api';


export interface PagedResponse<T> {
    content: T[];
    pageNo: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}


export interface GuestDto {
    firstname: string;
    lastname: string;
}

export interface GuestCreationRequest {
    roomId: number;
    checkInDate: string;
    checkOutDate: string;
    guests: GuestDto[];
}

export interface GuestDetail {
    id: number;
    firstname: string;
    lastname: string;
}

export interface GuestCreationResponse {
    voucherNumber: string;
    roomId: number;
    checkInDate: string;
    checkOutDate: string;
    guests: GuestDetail[];
}

export interface GuestQueryDetail {
    id: number;
    firstname: string;
    lastname: string;
    voucherNumber: string;
    roomNumber: string;
    hotelName: string;
    checkInDate: string;
    checkOutDate: string;
}

export interface GuestSearchParams {
    firstname?: string;
    lastname?: string;
    voucherNumber?: string;
    roomNumber?: string;
    hotelName?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    direction?: 'asc' | 'desc';
}

// --- Servis (Service) ---

// Backend'deki @RequestMapping("/api/guests") yapısına uyum için. 
// (api instance içerisinde baseURL: '/api' tanımlı olduğu varsayılmıştır)
const BASE_PATH = '/guests';

export const guestService = {

    // PagedResponse döner, @ModelAttribute arama parametrelerini query string olarak yollar
    getGuests: async (params?: GuestSearchParams): Promise<PagedResponse<GuestQueryDetail>> => {
        const response = await api.get<PagedResponse<GuestQueryDetail>>(BASE_PATH, { params });
        return response.data;
    },

    getGuestsByHotelId: async (hotelId: number): Promise<GuestQueryDetail[]> => {
        const response = await api.get<GuestQueryDetail[]>(`${BASE_PATH}/hotel/${hotelId}`);
        return response.data;
    },

    getGuestsByRoomId: async (roomId: number): Promise<GuestQueryDetail[]> => {
        const response = await api.get<GuestQueryDetail[]>(`${BASE_PATH}/room/${roomId}`);
        return response.data;
    },

    // Creation DTO'su alır ve döner. UUID backend tarafından üretilecek.
    createGuest: async (request: GuestCreationRequest): Promise<GuestCreationResponse> => {
        const response = await api.post<GuestCreationResponse>(`${BASE_PATH}/create`, request);
        return response.data;
    },

    deleteGuest: async (guestId: number): Promise<void> => {
        await api.delete(`${BASE_PATH}/${guestId}`);
    },

    cancelReservation: async (voucherNumber: string): Promise<void> => {
        await api.delete(`${BASE_PATH}/cancel-reservation/${voucherNumber}`);
    },

    // Güncelleme işlemi de Creation response tipi döner
    updateReservation: async (voucherNumber: string, request: GuestCreationRequest): Promise<GuestCreationResponse> => {
        const response = await api.put<GuestCreationResponse>(`${BASE_PATH}/reservation/${voucherNumber}`, request);
        return response.data;
    }
};