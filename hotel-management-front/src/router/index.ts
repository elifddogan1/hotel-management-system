import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import HotelView from '../views/HotelView.vue';


const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'hotels',
    component: HotelView
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;