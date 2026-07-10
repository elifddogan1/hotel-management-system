import api from './api';

// --- INTERFACES (DTO Mappings) ---

// Backend: HotelResponse
export interface HotelResponse {
    id: number; // Artık opsiyonel değil, backend response'unda her zaman dönecek
    name: string;
    location: string;
    contactInfo: string;
}

// Backend: HotelRequest.Creation
export interface CreateHotelRequest {
    name: string; // @NotBlank olduğu için zorunlu
    location?: string;
    contactInfo?: string;
}

// Backend: HotelRequest.Update
export interface UpdateHotelRequest {
    name: string; // @NotBlank olduğu için zorunlu
    location?: string;
    contactInfo?: string;
}

// Backend: HotelRequest.Search
export interface HotelSearchRequest {
    name?: string;
    location?: string;
    page?: number;       // default: 0
    size?: number;       // default: 10
    sortBy?: string;     // default: 'id'
    direction?: string;  // default: 'asc'
}

// Backend: PagedResponse<T>
export interface PagedResponse<T> {
    content: T[];
    pageNo: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

// --- API SERVICE ---

export const hotelService = {

    // Tüm otelleri listele (GET)
    // Controller: @GetMapping -> List<HotelResponse>
    getAllHotels: async (): Promise<HotelResponse[]> => {
        const response = await api.get<HotelResponse[]>('/hotels');
        return response.data;
    },

    // Sayfalı ve Filtreli Arama (GET)
    // Not: Controller dosyanızda bunu göremedim ama DTO'larınıza istinaden frontend metodunu hazırladım.
    // Backend'de örn: @GetMapping("/search") eklerseniz bu metot tam uyumlu çalışacaktır.
    searchHotels: async (params: HotelSearchRequest): Promise<PagedResponse<HotelResponse>> => {
        const response = await api.get<PagedResponse<HotelResponse>>('/hotels/search', { params });
        return response.data;
    },

    // ID'ye göre otel getir (GET)
    // Controller: @GetMapping("/{id}")
    getHotelById: async (id: number): Promise<HotelResponse> => {
        const response = await api.get<HotelResponse>(`/hotels/${id}`);
        return response.data;
    },

    // Yeni otel ekle (POST)
    // Controller: @PostMapping -> @RequestBody HotelRequest.Creation
    createHotel: async (data: CreateHotelRequest): Promise<HotelResponse> => {
        const response = await api.post<HotelResponse>('/hotels', data);
        return response.data;
    },

    // Otel güncelle (PUT)
    // Controller: @PutMapping("/{id}") -> @RequestBody HotelRequest.Update
    updateHotel: async (id: number, data: UpdateHotelRequest): Promise<HotelResponse> => {
        const response = await api.put<HotelResponse>(`/hotels/${id}`, data);
        return response.data;
    },

    // Otel sil (DELETE)
    // Controller: @DeleteMapping("/{id}")
    deleteHotel: async (id: number): Promise<void> => {
        await api.delete(`/hotels/${id}`);
    }
};