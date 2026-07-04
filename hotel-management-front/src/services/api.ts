import axios from 'axios';

const api = axios.create({
    baseURL: 'http://hotelmanagement.duckdns.org/api', // Dockerized Backend URL'imiz
    headers: {
        'Content-Type': 'application/json',
    },
});

export default api;