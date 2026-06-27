import { Bot } from "lucide-react";
import { useNavigate } from "react-router-dom";

export default function FloatingChatbot() {
    const navigate = useNavigate();

    return (
        <button
            onClick={() => navigate("/assistant")}
            className="
      fixed bottom-6 right-6
      z-[9999]
      w-14 h-14
      rounded-full
      bg-brand-600 text-white
      shadow-xl
      hover:scale-105
      transition"
        >
            <Bot className="w-6 h-6 mx-auto" />
        </button>
    );
}