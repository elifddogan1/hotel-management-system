import api from './api';

export interface Room {
    id?: number;
    roomNumber: string;
    roomType: string;
    maxCapacity: number;
    hotelId: number;
}

export interface RoomCreationRequest {
    roomNumber: string;
    roomType: string;
    maxCapacity: number;
    hotelId?: number;
}

export interface AvailableRoomSearchParams {
    hotelId?: number;
    numberOfPerson: number;
    checkInDate: string;
    checkOutDate: string;
}

export const roomService = {
    getAllRooms: async (): Promise<Room[]> => {
        const response = await api.get<Room[]>('/rooms/all');
        return response.data;
    },

    getRoomsByHotelId: async (hotelId: number): Promise<Room[]> => {
        const response = await api.get<Room[]>(`/rooms/hotel/${hotelId}`);
        return response.data;
    },

    getAvailableRooms: async (params: AvailableRoomSearchParams): Promise<Room[]> => {
        if (params.hotelId) {
            const response = await api.get<Room[]>('/rooms', {
                params: {
                    hotelId: params.hotelId,
                    numberOfPerson: params.numberOfPerson,
                    checkInDate: params.checkInDate,
                    checkOutDate: params.checkOutDate
                }
            });
            return response.data;
        } else {
            const response = await api.get<Room[]>('/rooms/available-all', {
                params: {
                    numberOfPerson: params.numberOfPerson,
                    checkInDate: params.checkInDate,
                    checkOutDate: params.checkOutDate
                }
            });
            return response.data;
        }
    },

    createRoomOnHotel: async (hotelId: number, room: RoomCreationRequest): Promise<Room> => {
        const response = await api.post<Room>(`/rooms/hotel/${hotelId}`, room);
        return response.data;
    },

    deleteRoom: async (id: number): Promise<void> => {
        await api.delete(`/rooms/${id}`);
    }
};