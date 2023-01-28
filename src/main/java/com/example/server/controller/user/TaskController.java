package com.example.server.controller.user;

import com.example.server.model.Task;
import com.example.server.model.User;
import com.example.server.repository.TaskRepository;
import com.example.server.repository.UserRepository;
import com.example.server.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user/task")
public class TaskController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TaskRepository taskRepository;

    private User getMyUser(){
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> myUser = userRepository.findByUsername(userDetails.getUsername());
        return myUser.get();
    }

    @GetMapping()
    public ResponseEntity<List<Task>> getAllTask(){
        User myUser = getMyUser();
        if(myUser!=null){
            List<Task> listTask = new ArrayList<>();
            List<Task> temp = taskRepository.findAll();
            for(Task a: temp){
                if(a.getUser_id()==myUser.getId()) listTask.add(a);
            }
            return new ResponseEntity<>(listTask, HttpStatus.OK);

        }else{
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping()
    public ResponseEntity<Task> addNewTask(@RequestBody Task task){
        User myUser = getMyUser();
        if(myUser!=null){
            task.setUser_id(myUser.getId());
            return new ResponseEntity<>(taskRepository.save(task), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("/{id_task}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id_task, @RequestBody Task task){
        Optional<Task> myTask = taskRepository.findById(id_task);
        User myUser = getMyUser();
        if(myTask.isPresent()){
            Task updateTask = myTask.get();
            if(updateTask.getUser_id()!=myUser.getId())
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            updateTask.setTitle(task.getTitle());
            updateTask.setBody(task.getBody());
            updateTask.setDone(task.getDone());
            return new ResponseEntity<>(taskRepository.save(updateTask), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @DeleteMapping("/{id_task}")
    public ResponseEntity<Task> deleteTask(@PathVariable Long id_task){
        Optional<Task> myTask = taskRepository.findById(id_task);
        User myUser = getMyUser();
        if(myTask.isPresent()){
            if(myTask.get().getUser_id()!=myUser.getId())
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            taskRepository.deleteById(id_task);
            return new ResponseEntity<>(HttpStatus.OK);
        }else
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
