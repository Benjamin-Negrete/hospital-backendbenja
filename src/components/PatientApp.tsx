import React, { useState, useEffect } from 'react';
import type { ChangeEvent, FormEvent } from 'react';
import type { Patient } from '../types/patient';
import { getPatients, createPatient, updatePatient } from '../services/patientService';
import axios from 'axios';

const initialFormState: Patient = {
  rut: '',
  firstName: '',
  lastName: '',
  birthDate: '',
  email: ''
};

export const PatientApp: React.FC = () => {
  const [patients, setPatients] = useState<Patient[]>([]);
  const [formData, setFormData] = useState<Patient>(initialFormState);
  const [isEditing, setIsEditing] = useState<boolean>(false);
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [msgStatus, setMsgStatus] = useState<string>('');

  useEffect(() => {
    fetchPatients();
  }, []);

  const fetchPatients = async () => {
    try {
      setLoading(true);
      const data = await getPatients();
      setPatients(data);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Error al conectar con la API de pacientes.');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const parsedDate = new Date(`${formData.birthDate}T00:00:00`);
    if (isNaN(parsedDate.getTime())) {
      setError('Por favor selecciona una fecha de nacimiento válida.');
      return;
    }

    const payload: Patient = {
      ...formData,
      birthDate: parsedDate.toISOString()
    };

    try {
      if (isEditing && formData.id) {
        await updatePatient(formData.id, payload);
        setSuccess('Paciente actualizado correctamente.');
      } else {
        await createPatient(payload);
        setSuccess('Paciente registrado correctamente. Se despachó notificación asíncrona a RabbitMQ.');
      }
      setFormData(initialFormState);
      setIsEditing(false);
      fetchPatients();
    } catch (err: any) {
      const backendMessage = err.response?.data?.message;
      setError(backendMessage || 'Ocurrió un error al guardar los datos.');
    }
  };

  const handleEdit = (patient: Patient) => {
    setFormData({
      id: patient.id,
      rut: patient.rut,
      firstName: patient.firstName,
      lastName: patient.lastName,
      birthDate: patient.birthDate ? patient.birthDate.split('T')[0] : '',
      email: patient.email
    });
    setIsEditing(true);
    setError('');
    setSuccess('');
  };

  const handleCancel = () => {
    setIsEditing(false);
    setFormData(initialFormState);
    setError('');
    setSuccess('');
  };

  // Disparadores rápidos de mensajería RabbitMQ para demostración
  const triggerRabbitMQMessage = async (type: 'notification' | 'ticket' | 'error') => {
    try {
      setMsgStatus('Enviando evento a RabbitMQ...');
      const baseUrl = import.meta.env.VITE_API_URL || (window.location.hostname === 'localhost' ? 'http://localhost:8080' : http://:8080);
      
      if (type === 'notification') {
        await axios.post(`${baseUrl}/api/v1/messages/notification`, {
          content: 'Recordatorio de control médico preventivo',
          recipient: 'paciente.hospital@duocuc.cl',
          forceError: false
        });
        setMsgStatus('✅ Notificación enviada a Direct Exchange -> Cola. Confirmada con ACK!');
      } else if (type === 'ticket') {
        await axios.post(`${baseUrl}/api/v1/messages/ticket/urgencias`, {
          content: 'Turno de Urgencias #U-104 asignado',
          recipient: 'box-3@hospital.cl',
          forceError: false
        });
        setMsgStatus('✅ Ticket enviado a Topic Exchange (hospital.ticket.*) -> Cola procesada con ACK!');
      } else if (type === 'error') {
        await axios.post(`${baseUrl}/api/v1/messages/notification`, {
          content: 'Mensaje intencional con fallo',
          recipient: 'auditoria@hospital.cl',
          forceError: true
        });
        setMsgStatus('🚨 Mensaje con error enviado -> Rechazado con NACK y movido a DLQ (Dead Letter Queue)!');
      }
      setTimeout(() => setMsgStatus(''), 7000);
    } catch (err: any) {
      setMsgStatus('Error al enviar mensaje a RabbitMQ');
    }
  };

  return (
    <div className="container py-2">
      {/* Tarjetas de Estadísticas / Dashboard */}
      <div className="row g-3 mb-4">
        <div className="col-md-3">
          <div className="card p-3 bg-white border-start border-primary border-4 shadow-sm">
            <small className="text-muted fw-bold text-uppercase">Pacientes Activos</small>
            <h3 className="fw-bold text-primary mb-0">{patients.length}</h3>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card p-3 bg-white border-start border-success border-4 shadow-sm">
            <small className="text-muted fw-bold text-uppercase">Direct Exchange</small>
            <h6 className="fw-bold text-success mb-0">cola.notificaciones</h6>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card p-3 bg-white border-start border-info border-4 shadow-sm">
            <small className="text-muted fw-bold text-uppercase">Topic Exchange</small>
            <h6 className="fw-bold text-info mb-0">hospital.ticket.*</h6>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card p-3 bg-white border-start border-warning border-4 shadow-sm">
            <small className="text-muted fw-bold text-uppercase">Tolerancia a Fallos</small>
            <h6 className="fw-bold text-warning mb-0">3 DLQs Activas</h6>
          </div>
        </div>
      </div>

      {/* Botones de acción rápida RabbitMQ */}
      <div className="card mb-4 bg-light border-0 shadow-sm p-3">
        <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
          <span className="fw-semibold text-secondary">⚡ Pruebas de Mensajería Asíncrona (RabbitMQ):</span>
          <div className="d-flex flex-wrap gap-2">
            <button className="btn btn-outline-primary btn-sm" onClick={() => triggerRabbitMQMessage('notification')}>
              🔔 Enviar Notificación (ACK)
            </button>
            <button className="btn btn-outline-info btn-sm text-dark" onClick={() => triggerRabbitMQMessage('ticket')}>
              🎫 Emitir Ticket Médico
            </button>
            <button className="btn btn-outline-danger btn-sm" onClick={() => triggerRabbitMQMessage('error')}>
              ⚠️ Simular Error y Derivar a DLQ
            </button>
          </div>
        </div>
        {msgStatus && (
          <div className="alert alert-info py-2 px-3 mt-3 mb-0 small">
            {msgStatus}
          </div>
        )}
      </div>

      {/* Mensajes de Alerta */}
      {error && <div className="alert alert-danger shadow-sm">{error}</div>}
      {success && <div className="alert alert-success shadow-sm">{success}</div>}

      <div className="row g-4">
        {/* Formulario */}
        <div className="col-lg-5">
          <div className="card shadow-sm h-100">
            <div className={`card-header text-white ${isEditing ? 'bg-warning text-dark' : 'bg-primary'} py-3`}>
              <h5 className="mb-0 fw-bold">{isEditing ? '✏️ Editar Paciente' : '➕ Registrar Paciente'}</h5>
            </div>
            <div className="card-body p-4">
              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label className="form-label fw-semibold">RUT</label>
                  <input
                    type="text"
                    className="form-control"
                    name="rut"
                    placeholder="12345678-9"
                    value={formData.rut}
                    onChange={handleInputChange}
                    required
                  />
                </div>

                <div className="mb-3">
                  <label className="form-label fw-semibold">Correo Electrónico</label>
                  <input
                    type="email"
                    className="form-control"
                    name="email"
                    placeholder="paciente@correo.com"
                    value={formData.email}
                    onChange={handleInputChange}
                    required
                  />
                </div>

                <div className="row g-2 mb-3">
                  <div className="col-6">
                    <label className="form-label fw-semibold">Nombre</label>
                    <input
                      type="text"
                      className="form-control"
                      name="firstName"
                      value={formData.firstName}
                      onChange={handleInputChange}
                      required
                    />
                  </div>
                  <div className="col-6">
                    <label className="form-label fw-semibold">Apellido</label>
                    <input
                      type="text"
                      className="form-control"
                      name="lastName"
                      value={formData.lastName}
                      onChange={handleInputChange}
                      required
                    />
                  </div>
                </div>

                <div className="mb-4">
                  <label className="form-label fw-semibold">Fecha de Nacimiento</label>
                  <input
                    type="date"
                    className="form-control"
                    name="birthDate"
                    value={formData.birthDate}
                    onChange={handleInputChange}
                    required
                  />
                </div>

                <div className="d-flex gap-2">
                  <button type="submit" className={`btn ${isEditing ? 'btn-warning text-dark' : 'btn-primary'} flex-grow-1 py-2 fw-semibold`}>
                    {isEditing ? 'Guardar Cambios' : 'Registrar Paciente'}
                  </button>
                  {isEditing && (
                    <button type="button" className="btn btn-secondary px-3" onClick={handleCancel}>
                      Cancelar
                    </button>
                  )}
                </div>
              </form>
            </div>
          </div>
        </div>

        {/* Tabla de Pacientes */}
        <div className="col-lg-7">
          <div className="card shadow-sm h-100">
            <div className="card-header bg-white py-3 d-flex justify-content-between align-items-center">
              <h5 className="mb-0 fw-bold text-dark">📋 Directorio de Pacientes</h5>
              <button className="btn btn-outline-secondary btn-sm" onClick={fetchPatients} disabled={loading}>
                {loading ? 'Cargando...' : '🔄 Actualizar'}
              </button>
            </div>
            <div className="card-body p-0">
              <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                  <thead className="table-light">
                    <tr>
                      <th className="ps-4">RUT</th>
                      <th>Nombre Completo</th>
                      <th>Email</th>
                      <th className="text-end pe-4">Acción</th>
                    </tr>
                  </thead>
                  <tbody>
                    {patients.length === 0 ? (
                      <tr>
                        <td colSpan={4} className="text-center py-5 text-muted">
                          No hay pacientes registrados aún en la base de datos.
                        </td>
                      </tr>
                    ) : (
                      patients.map((p) => (
                        <tr key={p.id || p.rut}>
                          <td className="ps-4 fw-bold">{p.rut}</td>
                          <td>{p.firstName} {p.lastName}</td>
                          <td className="text-muted small">{p.email}</td>
                          <td className="text-end pe-4">
                            <button className="btn btn-outline-primary btn-sm py-1 px-2" onClick={() => handleEdit(p)}>
                              Editar
                            </button>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
