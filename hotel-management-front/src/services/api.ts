import axios from 'axios';

const api = axios.create({
    baseURL: 'http://34.40.79.243:8080/api', // Dockerized Backend URL'imiz
    headers: {
        'Content-Type': 'application/json',
    },
});

export default api;