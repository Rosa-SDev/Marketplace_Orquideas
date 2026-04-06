import axios from 'axios';

// Instancia base de axios apuntando al backend
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

// Interceptor: antes de cada petición, adjunta el token JWT si existe
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;