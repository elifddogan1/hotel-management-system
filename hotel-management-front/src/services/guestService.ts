import api from './api';
import type { Room } from './roomService';

export interface GuestDto {
    firstname: string;
    lastname: string;
}

export interface GuestCreationRequest {
    voucherNumber: string;
    roomId: number;
    checkInDate: string;
    checkOutDate: string;
    guests: GuestDto[];
}

export interface Guest {
    id: number;
    firstname: string;
    lastname: string;
    voucherNumber: string;
    checkInDate: string;
    checkOutDate: string;
    room?: Room;
}

export interface GuestSearchParams {
    lastName?: string;
    voucherNumber?: string;
    sortBy?: string;
    direction?: 'asc' | 'desc';
}

export const guestService = {
    getGuestsByHotelId: async (hotelId: number): Promise<Guest[]> => {
        const response = await api.get<Guest[]>(`/guest/hotel/${hotelId}`);
        return response.data;
    },

    getGuestsByRoomId: async (roomId: number): Promise<Guest[]> => {
        const response = await api.get<Guest[]>(`/guest/room/${roomId}`);
        return response.data;
    },

    createGuest: async (request: GuestCreationRequest): Promise<Guest[]> => {
        if (request.voucherNumber == null || request.voucherNumber === "") {
            request.voucherNumber = "VCH-" + crypto.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        const response = await api.post<Guest[]>('/guest/create', request);
        return response.data;
    },


    getGuests: async (params?: GuestSearchParams): Promise<Guest[]> => {
        const response = await api.get<Guest[]>('/guest/search', { params });
        return response.data;
    },
    deleteGuest: async (guestId: number): Promise<void> => {
        await api.delete(`/guest/${guestId}`);
    },

    cancelReservation: async (voucherNumber: string): Promise<void> => {
        await api.delete(`/guest/cancel-reservation/${voucherNumber}`);
    },
    updateReservation: async (voucherNumber: string, request: GuestCreationRequest): Promise<Guest[]> => {
        const response = await api.put<Guest[]>(`/guest/reservation/${voucherNumber}`, request);
        return response.data;
    }
};