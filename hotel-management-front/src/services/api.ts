import axios from 'axios';

const api = axios.create({
    baseURL: 'https://hotelmanagement.duckdns.org/api', // HTTPS olarak güncelledik
    headers: {
        'Content-Type': 'application/json',
    },
});

export default api;