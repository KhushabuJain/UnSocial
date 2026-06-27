import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar.jsx'
import Navbar from './Navbar.jsx'
import FloatingChatbot from '../chatbot/FloatingChatbot.jsx'

export default function AppLayout() {
    const [sidebarOpen, setSidebarOpen] = useState(false)

    return (
        <div className="flex h-screen overflow-hidden dark:bg-surface bg-transparent">
            <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />

            <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
                <Navbar onMenuClick={() => setSidebarOpen(true)} />

                <main className="flex-1 overflow-y-auto p-5 lg:p-7 animate-fade-in">
                    <Outlet />
                </main>
            </div>

            {/* Floating AI Assistant */}
            <FloatingChatbot />
        </div>
    )
}