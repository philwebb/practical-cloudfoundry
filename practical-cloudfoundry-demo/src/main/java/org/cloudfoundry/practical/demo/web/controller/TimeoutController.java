/*
 * Copyright 2010-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cloudfoundry.practical.demo.web.controller;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Phillip Webb
 */
@Controller
public class TimeoutController {

	private Log logger = LogFactory.getLog(getClass());

	private static final int TOTAL_SECONDS_DELAY = 70;

	private static final long ONE_SECOND = 1000;

	@RequestMapping("/timeout/default")
	public ModelAndView timeoutDefault() {
		ModelAndView modelAndView = new ModelAndView("timeout");
		modelAndView.addObject("useshim", false);
		modelAndView.addObject("ajaxcall", "ajaxrequest");
		return modelAndView;
	}

	@RequestMapping("/timeout/drip")
	public ModelAndView timeouDrip() {
		ModelAndView modelAndView = new ModelAndView("timeout");
		modelAndView.addObject("useshim", false);
		modelAndView.addObject("ajaxcall", "ajaxdrip");
		return modelAndView;
	}

	@RequestMapping("/timeout/shim")
	public ModelAndView timeoutShim() {
		ModelAndView modelAndView = new ModelAndView("timeout");
		modelAndView.addObject("useshim", true);
		modelAndView.addObject("ajaxcall", "ajaxrequest");
		return modelAndView;
	}

	@RequestMapping("/timeout/ajaxrequest")
	@ResponseBody
	public String ajaxRequest() {
		this.logger.info("Long running task...");
		try {
			for (int i = 1; i <= TOTAL_SECONDS_DELAY; i++) {
				Thread.sleep(ONE_SECOND);
				this.logger.info(" Thinking..." + i);
			}
		} catch (InterruptedException e) {
		}
		return "Hello from the server";
	}

	@RequestMapping("/timeout/ajaxdrip")
	public void ajax(HttpServletResponse response) throws IOException {
		ServletOutputStream outputStream = response.getOutputStream();
		this.logger.info("Long running task...");
		try {
			for (int i = 1; i <= TOTAL_SECONDS_DELAY; i++) {
				Thread.sleep(ONE_SECOND);
				this.logger.info(" Thinking..." + i);
				outputStream.write('.');
				response.flushBuffer();
			}
		} catch (InterruptedException e) {
		}
		outputStream.write("Hello from the server".getBytes());
	}

}
