import api from './api';

export interface Product {
  id: string;
  title: string;
  description: string;
  category: string;
  price: number;
  quantity: number;
  unit: string;
  image_url?: string;
  status: string;
  created_at: string;
}

export interface BookingRequest {
  product_id: string;
  quantity: number;
}

export interface Booking {
  id: string;
  product_id: string;
  buyer_id: string;
  farmer_id: string;
  quantity: number;
  status: string;
  farmer_name: string;
  farmer_mobile: string;
  farmer_village: string;
  created_at: string;
}

// Farmer Product Management
export const farmerProducts = {
  create: async (userId: string, data: Partial<Product>) => {
    const response = await api.post(`/farmer/products?user_id=${userId}`, data);
    return response.data;
  },

  getAll: async (userId: string) => {
    const response = await api.get(`/farmer/products?user_id=${userId}`);
    return response.data;
  },

  getOne: async (userId: string, id: string) => {
    const response = await api.get(`/farmer/products/${id}?user_id=${userId}`);
    return response.data;
  },

  update: async (userId: string, id: string, data: Partial<Product>) => {
    const response = await api.put(`/farmer/products/${id}?user_id=${userId}`, data);
    return response.data;
  },

  delete: async (userId: string, id: string) => {
    const response = await api.delete(`/farmer/products/${id}?user_id=${userId}`);
    return response.data;
  },

  publish: async (userId: string, id: string) => {
    const response = await api.post(`/farmer/products/${id}/publish?user_id=${userId}`);
    return response.data;
  },
};

// Buyer Product Discovery
export const buyerProducts = {
  search: async (query: string, filters?: any) => {
    const response = await api.get('/buyer/search', { params: { query, ...filters } });
    return response.data;
  },

  getHome: async () => {
    const response = await api.get('/buyer/home');
    return response.data;
  },

  getOne: async (id: string) => {
    const response = await api.get(`/buyer/products/${id}`);
    return response.data;
  },
};

// Bookings
export const bookings = {
  create: async (userId: string, data: any) => {
    const response = await api.post(`/buyer/booking?user_id=${userId}`, data);
    return response.data;
  },

  getAll: async (userId: string) => {
    const response = await api.get(`/buyer/bookings?user_id=${userId}`);
    return response.data;
  },

  getFarmerBookings: async (userId: string) => {
    const response = await api.get(`/farmer/bookings?user_id=${userId}`);
    return response.data;
  },

  complete: async (userId: string, id: string) => {
    const response = await api.post(`/buyer/bookings/${id}/complete?user_id=${userId}`);
    return response.data;
  },

  report: async (userId: string, data: { booking_id: string, reason: string, description: string }) => {
    const response = await api.post(`/buyer/report?user_id=${userId}`, data);
    return response.data;
  }
};
