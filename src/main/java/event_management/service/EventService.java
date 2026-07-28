package event_management.service;

import event_management.dto.AlterarStatusRequestDto;
import event_management.dto.EventRequestDto;
import event_management.dto.EventResponseDto;
import event_management.enums.EventStatus;
import event_management.model.Category;
import event_management.model.Event;
import event_management.model.Users;
import event_management.repository.AttendeeRepository;
import event_management.repository.CategoryRepository;
import event_management.repository.EventRepository;
import event_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AttendeeRepository attendeeRepository;

    public Page<EventResponseDto> listar(Long categoriaId, EventStatus status,
                                          LocalDateTime dataInicio, LocalDateTime dataFim,
                                          String busca, Pageable pageable) {
        log.info("Listando eventos com filtros");

        Page<Event> eventos = eventRepository.filtrar(categoriaId, status, dataInicio, dataFim, busca, pageable);
        return eventos.map(this::toResponseDto);
    }

    public EventResponseDto buscarPorId(Long id) {
        log.info("Buscando evento por ID: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
        return toResponseDto(event);
    }

    @Transactional
    public EventResponseDto criar(EventRequestDto dto, String email) {
        log.info("Criando evento para usuário: {}", email);

        Category categoria = categoryRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        Users criador = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        EventStatus status = calcularStatus(dto.getDataHora());

        Event event = Event.builder()
                .titulo(dto.getTitulo())
                .descricao(dto.getDescricao())
                .dataHora(dto.getDataHora())
                .local(dto.getLocal())
                .status(status)
                .qtdVagas(dto.getQtdVagas())
                .categoria(categoria)
                .criador(criador)
                .build();

        event = eventRepository.save(event);
        log.info("Evento criado com ID: {}", event.getId());
        return toResponseDto(event);
    }

    @Transactional
    public EventResponseDto atualizar(Long id, EventRequestDto dto, String email) {
        log.info("Atualizando evento ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        Category categoria = categoryRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        event.setTitulo(dto.getTitulo());
        event.setDescricao(dto.getDescricao());
        event.setDataHora(dto.getDataHora());
        event.setLocal(dto.getLocal());
        event.setStatus(calcularStatus(dto.getDataHora()));
        event.setQtdVagas(dto.getQtdVagas());
        event.setCategoria(categoria);

        event = eventRepository.save(event);
        return toResponseDto(event);
    }

    @Transactional
    public void deletar(Long id) {
        log.info("Deletando evento ID: {}", id);
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Evento não encontrado");
        }
        eventRepository.deleteById(id);
    }

    @Transactional
    public EventResponseDto alterarStatus(Long id, AlterarStatusRequestDto dto) {
        log.info("Alterando status do evento ID: {} para {}", id, dto.getStatus());

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        event.setStatus(dto.getStatus());
        event = eventRepository.save(event);
        return toResponseDto(event);
    }

    @Transactional
    public void atualizarStatusAutomaticamente() {
        log.info("Atualizando status dos eventos automaticamente");
        LocalDateTime now = LocalDateTime.now();

        eventRepository.findByDataHoraBeforeAndStatusNot(now.minusHours(2), EventStatus.FINISHED)
                .forEach(event -> {
                    event.setStatus(EventStatus.FINISHED);
                    eventRepository.save(event);
                });
    }

    private EventStatus calcularStatus(LocalDateTime dataHora) {
        LocalDateTime now = LocalDateTime.now();

        if (dataHora.isAfter(now)) {
            return EventStatus.UPCOMING;
        } else if (dataHora.isBefore(now) && dataHora.plusHours(2).isAfter(now)) {
            return EventStatus.ONGOING;
        } else {
            return EventStatus.FINISHED;
        }
    }

    private EventResponseDto toResponseDto(Event event) {
        long totalInscritos = attendeeRepository.countByEventoId(event.getId());
        int vagasRestantes = event.getQtdVagas() - (int) totalInscritos;

        return EventResponseDto.builder()
                .id(event.getId())
                .titulo(event.getTitulo())
                .descricao(event.getDescricao())
                .dataHora(event.getDataHora())
                .local(event.getLocal())
                .status(event.getStatus())
                .qtdVagas(event.getQtdVagas())
                .vagasRestantes(Math.max(vagasRestantes, 0))
                .categoria(event.getCategoria().getNome())
                .categoriaId(event.getCategoria().getId())
                .criador(event.getCriador().getNome())
                .criadorId(event.getCriador().getId())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
