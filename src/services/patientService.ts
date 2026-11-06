import axios from 'axios';
import type { Patient, ApiResponseDTO } from '../types/patient';

const getBackendUrl = () => {
  if (import.meta.env.VITE_API_URL) return import.meta.env.VITE_API_URL;
  if (typeof window !== 'undefined' && window.location.hostname !== 'localhost') {
    return 'http://' + window.location.hostname + ':8080';
  }
  return 'http://localhost:8080';
};

const BASE_URL = getBackendUrl();
const API_URL = `${BASE_URL.replace(/\/$/, '')}/api/v1/patients`;

const AUTHORITY = "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OwELeaTvI";
const CLIENT_ID = "3f49r0c3vj5qfnslmfs5v9mvdi";

const getAuthHeaders = () => {
  const oidcStorageKey = `oidc.user:${AUTHORITY}:${CLIENT_ID}`;
  const oidcData = sessionStorage.getItem(oidcStorageKey) || localStorage.getItem(oidcStorageKey);

  if (oidcData) {
    try {
      const user = JSON.parse(oidcData);
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
