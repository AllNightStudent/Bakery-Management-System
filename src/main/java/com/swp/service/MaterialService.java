package com.swp.service;

import com.swp.dto.MaterialDTO;
import com.swp.entity.MaterialEntity;
import com.swp.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MaterialService {

	private final MaterialRepository materialRepository;

	public Page<MaterialDTO> getMaterials(String keyword, int page, int size, String sortBy, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase("asc")
				? Sort.by(sortBy).ascending()
				: Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page, size, sort);
		if (keyword != null && !keyword.trim().isEmpty()) {
			return materialRepository.searchByName(keyword.trim(), pageable).map(this::toDTO);
		}
		return materialRepository.findAll(pageable).map(this::toDTO);
	}

	public MaterialDTO getById(Long id) {
		MaterialEntity entity = materialRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu với ID: " + id));
		return toDTO(entity);
	}

	@Transactional
	public MaterialDTO create(MaterialDTO dto) {
		if (materialRepository.existsByName(dto.getName())) {
			throw new RuntimeException("Tên nguyên liệu đã tồn tại: " + dto.getName());
		}
		MaterialEntity entity = MaterialEntity.builder()
				.name(dto.getName())
				.stock(dto.getStock())
				.unit(dto.getUnit())
				.cost(dto.getCost())
				.expiryDate(dto.getExpiryDate())
				.build();
		return toDTO(materialRepository.save(entity));
	}

	@Transactional
	public MaterialDTO update(Long id, MaterialDTO dto) {
		MaterialEntity entity = materialRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu với ID: " + id));

		entity.setName(dto.getName());
		entity.setStock(dto.getStock());
		entity.setUnit(dto.getUnit());
		entity.setCost(dto.getCost());
		entity.setExpiryDate(dto.getExpiryDate());
		return toDTO(materialRepository.save(entity));
	}

	@Transactional
	public void delete(Long id) {
		if (!materialRepository.existsById(id)) {
			throw new RuntimeException("Không tìm thấy nguyên liệu với ID: " + id);
		}
		materialRepository.deleteById(id);
	}

	private MaterialDTO toDTO(MaterialEntity entity) {
		return MaterialDTO.builder()
				.materialId(entity.getMaterialId())
				.name(entity.getName())
				.stock(entity.getStock())
				.unit(entity.getUnit())
				.cost(entity.getCost())
				.expiryDate(entity.getExpiryDate())
				.build();
	}
}


