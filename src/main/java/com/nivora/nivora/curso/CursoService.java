package com.nivora.nivora.curso;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CursoService {

    private final CursoRepository cursos;

    public CursoService(CursoRepository cursos) {
        this.cursos = cursos;
    }

    @Transactional(readOnly = true)
    public List<CursoResponse> listar() {
        return cursos.findAll(Sort.by("id")).stream().map(CursoResponse::de).toList();
    }
}
