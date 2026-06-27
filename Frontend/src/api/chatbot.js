import api from './axios.js'
export const askChatbot     = (message) => api.post('/chatbot/ask', { message })
export const getChatHistory = ()        => api.get('/chatbot/history')
export const clearChatHistory = ()      => api.delete('/chatbot/history')
