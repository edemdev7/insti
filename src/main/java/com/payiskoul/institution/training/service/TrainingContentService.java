package com.payiskoul.institution.training.service;

import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.program.model.OfferType;
import com.payiskoul.institution.program.model.TrainingOffer;
import com.payiskoul.institution.program.repository.TrainingOfferRepository;
import com.payiskoul.institution.training.dto.*;
import com.payiskoul.institution.training.model.*;
import com.payiskoul.institution.training.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service pour la gestion du contenu des formations (sections, leçons, quiz)
 */
@Service
@