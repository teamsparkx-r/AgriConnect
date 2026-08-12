import api from './api';

export interface RegisterRequest {
  mobile: string;
  password: string;
  full_name: string;
}

export interface LoginRequest {
  mobile_number: string;
  password: string;
}

export interface OTPVerifyRequest {
  mobile_number: string;
  otp_code: string;
}

export interface AuthResponse {
  access_token: string;
  refresh_token: string;
  user: {
    id: string;
    role: string;
    mobile: string;
    full_name: string;
  };
}

export const appAuth = {
  login: async (data: LoginRequest) => {
    const response = await api.post('/auth/login', data);
    return response.data;
  },

  verifyOTP: async (data: OTPVerifyRequest) => {
    const response = await api.post('/auth/verify-otp', data);
    return response.data;
  },
};

// Farmer Auth
export const farmerAuth = {
  register: async (data: RegisterRequest & { location: string }) => {
    const response = await api.post('/farmer/register', data);
    return response.data;
  },

  verifyOTP: async (data: OTPVerifyRequest) => {
    const response = await api.post('/farmer/verify-otp', data);
    return response.data;
  },

  login: async (data: LoginRequest) => {
    const response = await api.post('/farmer/login', data);
    return response.data;
  },
};

// Buyer Auth
export const buyerAuth = {
  register: async (data: RegisterRequest & { location: string }) => {
    const response = await api.post('/buyer/register', data);
    return response.data;
  },

  verifyOTP: async (data: OTPVerifyRequest) => {
    const response = await api.post('/buyer/verify-otp', data);
    return response.data;
  },

  login: async (data: LoginRequest) => {
    const response = await api.post('/buyer/login', data);
    return response.data;
  },
};
