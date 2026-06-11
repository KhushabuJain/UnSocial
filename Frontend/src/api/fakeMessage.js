import api from './axios.js'
export const getTemplates       = ()          => api.get('/fake-messages/templates')
export const createTemplate     = (data)      => api.post('/fake-messages/templates', data)
export const updateTemplate     = (id, data)  => api.put(`/fake-messages/templates/${id}`, data)
export const deleteTemplate     = (id)        => api.delete(`/fake-messages/templates/${id}`)
export const setDefaultTemplate = (id)        => api.patch(`/fake-messages/templates/${id}/default`)
export const triggerFakeMessage = (data)      => api.post('/fake-messages/trigger', data)
