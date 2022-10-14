package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.Links.Link;
import com.PMVaadin.PMVaadin.Entities.ProjectTask.ProjectTask;
import com.PMVaadin.PMVaadin.Repositories.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LinkServiceImpl implements LinkService {

    private LinkRepository linkRepository;

    @Autowired
    public void setLinkRepository(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Override
    public List<Link> getAllLinks() {
        return linkRepository.findAll();
    }

    @Override
    public List<Link> getProjectTaskLinks(ProjectTask projectTask) {
        return linkRepository.findAllByProjectTaskId(projectTask.getId());
    }

}
