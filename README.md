# UnSocial

## Project Overview

UnSocial is a personal safety and social escape platform designed to help users discreetly exit uncomfortable situations and access emergency assistance when needed. The platform combines safety-focused features with social assistance tools to provide users with quick and reliable support during emergencies or uncomfortable social interactions.

## Problem Statement

Many individuals encounter situations where they feel unsafe, uncomfortable, or require immediate assistance but cannot openly ask for help. Traditional safety applications primarily focus on emergency response and often overlook everyday social challenges that people face.

## Solution

UnSocial addresses these challenges through a collection of safety and assistance features:

* Fake Message Generator
* Emergency Contact Management
* SOS Alert System
* Safety Timer
* Live Location Sharing
* User Authentication and Secure Access

## Features Implemented

### Authentication Module

* User Registration
* User Login
* JWT-Based Authentication
* Secure API Access

### Emergency Contacts Module

* Add Emergency Contact
* View Emergency Contacts
* Update Contact Information
* Delete Contacts

### Fake Message Module

* Create Fake Message Templates
* View Saved Templates
* Delete Templates

### User Management

* User Profile Management
* Account Settings

## Technology Stack

### Frontend

* React.js
* TypeScript
* Tailwind CSS
* Axios
* React Router

### Backend

* Java
* Spring Boot
* Spring Security
* JWT Authentication
* Maven

### Database

* MySQL

## Project Architecture

```text
Frontend (React)
        |
        v
REST APIs
        |
        v
Spring Boot Backend
        |
        v
MySQL Database
```

## Project Status

### Completed

* Backend Development
* Database Design
* Authentication System
* Emergency Contacts Module
* Fake Message Module
* API Testing with Postman

### In Progress

* Frontend Development
* UI/UX Design
* API Integration

## Getting Started

### Backend Setup

```bash
git clone <repository-url>
cd unsocial
mvn spring-boot:run
```

Backend will run on:

```text
http://localhost:8080
```

### Frontend Setup

```bash
cd unsocial-frontend
npm install
npm run dev
```

Frontend will run on:

```text
http://localhost:5173
```

## API Base URL

```text
http://localhost:8085/api
```

## Future Enhancements

* Fake Call Generator
* Real-Time SOS Notifications
* Live Location Tracking
* Mobile Application
* Push Notifications
* AI-Based Safety Recommendations

## Author

Khushabu Jain

GitHub: https://github.com/KhushabuJain
