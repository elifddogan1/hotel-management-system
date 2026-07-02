import { createRouter, createWebHashHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import HotelView from '../views/HotelView.vue';
import HotelDetailView from '../views/HotelDetailView.vue';
import RoomView from '../views/RoomView.vue';
import GuestView from '../views/GuestView.vue';
import ReservationView from '@/views/ReservationView.vue';

const routes: Array<RouteRecordRaw> = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/hotels', name: 'hotels', component: HotelView },
  { path: '/hotels/:id', name: 'hotel-detail', component: HotelDetailView },
  { path: '/rooms', name: 'rooms', component: RoomView },
  { path: '/reservation', name: 'reservation', component: ReservationView }, // YENİ ROTA
  { path: '/guests', name: 'guests', component: GuestView },
  {
    path: '/hotels/:hotelId/rooms/:roomId',
    name: 'room-detail',
    component: () => import('../views/RoomDetailView.vue')
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

export default router;