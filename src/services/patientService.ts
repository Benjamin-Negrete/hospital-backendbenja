import axios from 'axios';
import type { Patient, ApiResponseDTO } from '../types/patient';

// Si la variable no existe en local, usará localhost
const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Aseguramos la barra '/' entre la URL base y el endpoint
const API_URL = `${BASE_URL.replace(/\/$/, '')}/api/v1/patients`;

const AUTHORITY = import.meta.env.VITE_COGNITO_AUTHORITY || "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OwELeaTvI";
const CLIENT_ID = import.meta.env.VITE_COGNITO_CLIENT_ID || "3f49r0c3vj5qfnslmfs5v9mvdi";

const getAuthHeaders = () => {
  const oidcStorageKey = `oidc.user:${AUTHORITY}:${CLIENT_ID}`;
  
  // Buscar en sessionStorage primero, luego en localStorage
  const oidcData = sessionStorage.getItem(oidcStorageKey) || localStorage.getItem(oidcStorageKey);

  if (oidcData) {
    try {
      const user = JSON.parse(oidcData);
      // Usar id_token o access_token entregado por Cognito
      const token = user.id_token || user.access_token;
      
      if (token) {
        return {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        };
      }
    } catch (e) {
      console.error('Error al parsear el token de Cognito', e);
    }
  }

  console.warn('No se encontró el token de Cognito en los storages.');
  return {};
};

export const getPatients = async (): Promise<Patient[]> => {
  const response = await axios.get<ApiResponseDTO<Patient[]>>(API_URL, getAuthHeaders());
  if (response.status === 204) return [];
  return response.data.data || [];
};

export const createPatient = async (patientData: Patient): Promise<Patient> => {
  const response = await axios.post<ApiResponseDTO<Patient>>(API_URL, patientData, getAuthHeaders());
  return response.data.data;
};

export const updatePatient = async (id: number, patientData: Patient): Promise<Patient> => {
  const response = await axios.put<ApiResponseDTO<Patient>>(`${API_URL}/${id}`, patientData, getAuthHeaders());
  return response.data.data;
};