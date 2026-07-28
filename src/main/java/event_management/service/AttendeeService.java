package event_management.service;

import event_management.dto.AttendeeRequestDto;
import event_management.dto.AttendeeResponseDto;
import event_management.dto.InscricaoRequestDto;
import event_management.model.Attendee;
import event_management.model.Event;
import event_management.enums.EventStatus;
import event_management.repository.AttendeeRepository;
import event_management.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendeeService {

    private final AttendeeRepository attendeeRepository;
    private final EventRepository eventRepository;

    public List<AttendeeResponseDto> listarPorEvento(Long eventoId) {
        log.info("Listando participantes do evento ID: {}", eventoId);

        if (!eventRepository.existsById(eventoId)) {
            throw new RuntimeException("Evento não encontrado");
        }

        return attendeeRepository.findByEventoId(eventoId).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional
    public AttendeeResponseDto inscrever(Long eventoId, InscricaoRequestDto dto) {
        log.info("Inscrevendo participante {} no evento ID: {}", dto.getEmail(), eventoId);

        Event event = eventRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new RuntimeException("Evento cancelado");
        }

        if (event.getStatus() == EventStatus.FINISHED) {
            throw new RuntimeException("Evento já encerrado");
        }

        long totalInscritos = attendeeRepository.countByEventoId(eventoId);
        if (totalInscritos >= event.getQtdVagas()) {
            throw new RuntimeException("Evento sem vagas disponíveis");
        }

        if (attendeeRepository.existsByEmailAndEventoId(dto.getEmail(), eventoId)) {
            throw new RuntimeException("Email já inscrito neste evento");
        }

        Attendee attendee = Attendee.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .telefone(dto.getTelefone())
                .evento(event)
                .build();

        attendee = attendeeRepository.save(attendee);
        log.info("Participante inscrito com ID: {}", attendee.getId());
        return toResponseDto(attendee);
    }

    @Transactional
    public AttendeeResponseDto cadastrar(Long eventoId, AttendeeRequestDto dto) {
        log.info("Admin cadastrando participante {} no evento ID: {}", dto.getEmail(), eventoId);

        Event event = eventRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        long totalInscritos = attendeeRepository.countByEventoId(eventoId);
        if (totalInscritos >= event.getQtdVagas()) {
            throw new RuntimeException("Evento sem vagas disponíveis");
        }

        if (attendeeRepository.existsByEmailAndEventoId(dto.getEmail(), eventoId)) {
            throw new RuntimeException("Email já inscrito neste evento");
        }

        Attendee attendee = Attendee.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .telefone(dto.getTelefone())
                .evento(event)
                .build();

        attendee = attendeeRepository.save(attendee);
        return toResponseDto(attendee);
    }

    @Transactional
    public void deletar(Long eventoId, Long participanteId) {
        log.info("Removendo participante ID: {} do evento ID: {}", participanteId, eventoId);

        if (!eventRepository.existsById(eventoId)) {
            throw new RuntimeException("Evento não encontrado");
        }

        Attendee attendee = attendeeRepository.findById(participanteId)
                .orElseThrow(() -> new RuntimeException("Participante não encontrado"));

        if (!attendee.getEvento().getId().equals(eventoId)) {
            throw new RuntimeException("Participante não pertence a este evento");
        }

        attendeeRepository.delete(attendee);
    }

    private AttendeeResponseDto toResponseDto(Attendee attendee) {
        return AttendeeResponseDto.builder()
                .id(attendee.getId())
                .nome(attendee.getNome())
                .email(attendee.getEmail())
                .telefone(attendee.getTelefone())
                .createdAt(attendee.getCreatedAt())
                .build();
    }
}
