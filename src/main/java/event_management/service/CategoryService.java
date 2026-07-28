package event_management.service;

import event_management.dto.CategoryRequestDto;
import event_management.dto.CategoryResponseDto;
import event_management.model.Category;
import event_management.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponseDto> listar() {
        log.info("Listando categorias");
        return categoryRepository.findAll().stream()
                .map(this::toResponseDto)
                .toList();
    }

    public CategoryResponseDto buscarPorId(Long id) {
        log.info("Buscando categoria por ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
        return toResponseDto(category);
    }

    @Transactional
    public CategoryResponseDto criar(CategoryRequestDto dto) {
        log.info("Criando categoria: {}", dto.getNome());

        if (categoryRepository.existsByNome(dto.getNome())) {
            throw new RuntimeException("Categoria já existe");
        }

        Category category = Category.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .build();

        category = categoryRepository.save(category);
        return toResponseDto(category);
    }

    @Transactional
    public CategoryResponseDto atualizar(Long id, CategoryRequestDto dto) {
        log.info("Atualizando categoria ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        category.setNome(dto.getNome());
        category.setDescricao(dto.getDescricao());

        category = categoryRepository.save(category);
        return toResponseDto(category);
    }

    @Transactional
    public void deletar(Long id) {
        log.info("Deletando categoria ID: {}", id);
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Categoria não encontrada");
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponseDto toResponseDto(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .nome(category.getNome())
                .descricao(category.getDescricao())
                .build();
    }
}
