import { Routes, Route, Navigate } from 'react-router-dom'
import Login               from './pages/auth/Login.jsx'
import Register            from './pages/auth/Register.jsx'
import Dashboard           from './pages/dashboard/Dashboard.jsx'
import ContactsPage        from './pages/contacts/ContactsPage.jsx'
import FakeCallPage        from './pages/fakecall/FakeCallPage.jsx'
import FakeMessagePage     from './pages/fakemessage/FakeMessagePage.jsx'
import SosPage             from './pages/sos/SosPage.jsx'
import TrackingPage        from './pages/tracking/TrackingPage.jsx'
import TimerPage           from './pages/timer/TimerPage.jsx'
import ChatbotPage         from './pages/chatbot/ChatbotPage.jsx'
import PublicTrackingPage  from './pages/tracking/TrackingPage.jsx'
import AppLayout           from './components/layout/AppLayout.jsx'
import ProtectedRoute      from './routes/ProtectedRoute.jsx'

export default function App() {
    return (
        <Routes>
            {/* Public routes */}
            <Route path="/login"    element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/track/:token" element={<PublicTrackingPage />} />

            {/* Protected app routes */}
            <Route element={<ProtectedRoute />}>
                <Route element={<AppLayout />}>
                    <Route path="/dashboard"    element={<Dashboard />} />
                    <Route path="/contacts"     element={<ContactsPage />} />
                    <Route path="/fake-call"    element={<FakeCallPage />} />
                    <Route path="/fake-message" element={<FakeMessagePage />} />
                    <Route path="/sos"          element={<SosPage />} />
                    <Route path="/tracking"     element={<TrackingPage />} />
                    <Route path="/timer"        element={<TimerPage />} />
                <Route path="/assistant"    element={<ChatbotPage />} />

                </Route>
            </Route>

            <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
    )
}
