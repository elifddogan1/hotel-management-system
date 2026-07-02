# Build aşaması
FROM node:20-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Nginx ile servis etme aşaması
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
# Yeni konfigürasyonu varsayılan Nginx konfigürasyonunun üzerine yazıyoruz:
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
