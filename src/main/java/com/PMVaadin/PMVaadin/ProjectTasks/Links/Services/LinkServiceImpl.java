package com.PMVaadin.PMVaadin.ProjectTasks.Links.Services;

import com.PMVaadin.PMVaadin.ProjectTasks.Links.Entities.Link;
import com.PMVaadin.PMVaadin.ProjectTasks.Entity.ProjectTask;
import com.PMVaadin.PMVaadin.ProjectTasks.Links.Repositories.LinkRepository;
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
    public List<Link> getLinks(ProjectTask projectTask) {
        return linkRepository.findAllByProjectTaskId(projectTask.getId());
    }

}
