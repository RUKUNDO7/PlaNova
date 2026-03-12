package com.taskmanagement.app.config;

import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.Board;
import com.taskmanagement.app.model.BoardColumn;
import com.taskmanagement.app.model.ColumnStatus;
import com.taskmanagement.app.model.Project;
import com.taskmanagement.app.model.Task;
import com.taskmanagement.app.model.TaskLabel;
import com.taskmanagement.app.model.UserRole;
import com.taskmanagement.app.repository.BoardColumnRepository;
import com.taskmanagement.app.repository.BoardRepository;
import com.taskmanagement.app.repository.ProjectRepository;
import com.taskmanagement.app.repository.TaskLabelRepository;
import com.taskmanagement.app.repository.TaskRepository;
import com.taskmanagement.app.service.AppUserService;
import com.taskmanagement.app.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AppUserService appUserService;
    private final AuthService authService;
    private final ProjectRepository projectRepository;
    private final BoardRepository boardRepository;
    private final BoardColumnRepository columnRepository;
    private final TaskLabelRepository labelRepository;
    private final TaskRepository taskRepository;

    public DataInitializer(AppUserService appUserService,
                           AuthService authService,
                           ProjectRepository projectRepository,
                           BoardRepository boardRepository,
                           BoardColumnRepository columnRepository,
                           TaskLabelRepository labelRepository,
                           TaskRepository taskRepository) {
        this.appUserService = appUserService;
        this.authService = authService;
        this.projectRepository = projectRepository;
        this.boardRepository = boardRepository;
        this.columnRepository = columnRepository;
        this.labelRepository = labelRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public void run(String... args) {
        log.debug("Seeding user directory");
        AppUser admin = authService.ensureAdminUser("Platform Admin", "AdminPass#1");
        AppUser ops = appUserService.ensureSystemUser("ops@task.local", "Operations Lead", UserRole.USER, "OpsPass#1");
        AppUser marketing = appUserService.ensureSystemUser("marketing@task.local", "Marketing Specialist", UserRole.USER, "MarketPass#1");

        if (projectRepository.count() == 0) {
            log.debug("Seeding PlaNova workspace");
            Project project = new Project();
            project.setName("PlaNova Launch");
            project.setDescription("Cross-functional delivery plan for product launch milestones.");
            project.setOwner(admin);
            project.setMembers(new HashSet<>(List.of(admin, ops, marketing)));
            projectRepository.save(project);

            Board board = new Board();
            board.setName("Launch Board");
            board.setPosition(0);
            board.setProject(project);
            boardRepository.save(board);

            BoardColumn todo = new BoardColumn();
            todo.setName("To Do");
            todo.setStatus(ColumnStatus.TODO);
            todo.setPosition(0);
            todo.setBoard(board);
            columnRepository.save(todo);

            BoardColumn inProgress = new BoardColumn();
            inProgress.setName("In Progress");
            inProgress.setStatus(ColumnStatus.IN_PROGRESS);
            inProgress.setPosition(1);
            inProgress.setBoard(board);
            columnRepository.save(inProgress);

            BoardColumn done = new BoardColumn();
            done.setName("Done");
            done.setStatus(ColumnStatus.DONE);
            done.setPosition(2);
            done.setBoard(board);
            columnRepository.save(done);

            TaskLabel labelDesign = new TaskLabel();
            labelDesign.setName("Design");
            labelDesign.setColor("#f59e0b");
            labelDesign.setProject(project);
            labelRepository.save(labelDesign);

            TaskLabel labelEngineering = new TaskLabel();
            labelEngineering.setName("Engineering");
            labelEngineering.setColor("#0ea5e9");
            labelEngineering.setProject(project);
            labelRepository.save(labelEngineering);

            TaskLabel labelMarketing = new TaskLabel();
            labelMarketing.setName("Marketing");
            labelMarketing.setColor("#ec4899");
            labelMarketing.setProject(project);
            labelRepository.save(labelMarketing);
        }

        List<Task> unassigned = taskRepository.findByColumnIsNull();
        if (!unassigned.isEmpty()) {
            BoardColumn fallback = columnRepository.findAll().stream().findFirst().orElse(null);
            if (fallback != null) {
                unassigned.forEach(task -> task.setColumn(fallback));
                taskRepository.saveAll(unassigned);
            }
        }
    }
}