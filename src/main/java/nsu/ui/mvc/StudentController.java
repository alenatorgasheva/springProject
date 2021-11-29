/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package nsu.ui.mvc;

import javax.validation.Valid;

import nsu.ui.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nsu.ui.Student;
import nsu.ui.StudentRepository;

/**
 * @author Rob Winch
 */
@Controller
@RequestMapping("/")
public class StudentController {
	private final StudentRepository studentRepository;

	@Autowired
	public StudentController(StudentRepository studentRepository) {
		this.studentRepository = studentRepository;
	}

	@RequestMapping
	public ModelAndView list() {
		Iterable<Student> students = this.studentRepository.findAllStudents();
		return new ModelAndView("students/list", "students", students);
	}

	@RequestMapping(params = "sorted")
	public ModelAndView sorted() {
		Iterable<Group> groups = this.studentRepository.findAllGroups();
		return new ModelAndView("students/sorted", "groups", groups);
	}

	@RequestMapping("{id}")
	public ModelAndView view(@PathVariable("id") Student student) {
		return new ModelAndView("students/view", "student", student);
	}

	@RequestMapping(params = "form", method = RequestMethod.GET)
	public String createForm(@ModelAttribute Student student) {
		return "students/form";
	}

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView create(@Valid Student student, BindingResult result,
                               RedirectAttributes redirect) {
		if (result.hasErrors()) {
			return new ModelAndView("students/form", "formErrors", result.getAllErrors());
		}
		this.studentRepository.updateDB(student);
		redirect.addFlashAttribute("globalStudent", "Successfully added a new student");
		return new ModelAndView("redirect:/{student.id}", "student.id", student.getId());
	}

	@RequestMapping("foo")
	public String foo() {
		throw new RuntimeException("Expected exception in controller");
	}

}
