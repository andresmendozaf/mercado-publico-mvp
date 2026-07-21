# 🚀 MVP Mercado Público - Analytics & Engine

Backend desacoplado desarrollado en **Java 21 con Spring Boot 3**, diseñado para la sincronización, persistencia y análisis de licitaciones gubernamentales de Chile (Mercado Público).

---

## 📂 Estructura y Mapa del Proyecto

```text
src/main/java/com/mercadopublico/mvp/
 ├── config/       # Beans globales (RestTemplate, CORS, etc.)
 ├── controller/   # Controladores REST (/api/licitaciones)
 ├── dto/          # Mapeo de DTOs deserializados con Java Records
 ├── model/        # Entidades JPA (PostgreSQL) y Enums (EstadoLicitacion)
 ├── repository/   # Interfaces de persistencia Spring Data JPA
 └── service/      # Lógica de negocio e integración con la API de Mercado Público
```
---

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Java 21
* **Framework:** Spring Boot 3 (Spring Data JPA, RestTemplate/RestClient)
* **Base de Datos:** PostgreSQL 16
* **Contenedores:** Docker / Podman & Docker Compose
* **Calidad de Código:** Estándares de SonarQube & DTOs usando Java Records
* **Control de Versiones:** Git con estrategia Gitflow acotada y Conventional Commits

---

## ⚙️ Requisitos Previos

* **JDK 21** instalado.
* **Docker Engine** y **Docker Compose** activos.
* Cuenta y ticket/API Key activa en [Mercado Público](https://api.mercadopublico.cl).

---

## 🚀 Guía de Inicio Rápido (Despliegue Local)

### 1. Clonar el repositorio
```bash
git clone https://github.com/andresmendozaf/mercado-publico-mvp.git
cd mercado-publico-mvp