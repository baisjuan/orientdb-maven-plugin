package org.carlspring.maven.orientdb;

import java.io.File;
import java.net.BindException;

/**
 * Copyright 2016 Carlspring Consulting & Development Ltd.
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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author Martin Todorov (carlspring@gmail.com)
 * @author Juan Ignacio Bais (bais.juan@gmail.com)
 */
@Mojo(name = "start", requiresProject = false)
public class StartOrientDBMojo
        extends AbstractOrientDBMojo
{
	
    /**
     * Whether to fail, if there's already something running on the port.
     */
    @Parameter(property = "orientdb.fail.if.already.running", defaultValue = "true")
    private boolean failIfAlreadyRunning;


    @Override
    public void doExecute()
            throws MojoExecutionException, MojoFailureException
    {
        try
        {
            try
            {
                getLog().info("Starting the OrientDB server ...");
                server.startup(new File(configurationFile));
                server.activate();
            }
            catch (Exception e)
            {
                if (e instanceof BindException)
                {
                    if (failIfAlreadyRunning)
                    {
                        throw new MojoExecutionException("Failed to start the OrientDB server, port already open!", e);
                    }
                    else
                    {
                        getLog().info("OrientDB is already running.");
                    }
                }
                else
                {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }

            if (server != null)
            {
                long maxSleepTime = 60000;
                long sleepTime = 0;
                boolean pong = false;

                while (!pong && sleepTime < maxSleepTime)
                {
                        pong = server.isActive();
                        sleepTime += 1000;
                        Thread.sleep(1000);
                }

                if (pong)
                {
                    getLog().info("OrientDB ping-pong: [OK]");
                }
                else
                {
                    getLog().info("OrientDB ping-pong: [FAILED]");
                    throw new MojoFailureException("Failed to start the OServer." +
                                                   " The server did not respond with a pong withing 60 seconds.");
                }
            }
            else
            {
                throw new MojoExecutionException("Failed to start the OrientDB server!");
            }
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
    
    

    public boolean isFailIfAlreadyRunning()
    {
        return failIfAlreadyRunning;
    }

    public void setFailIfAlreadyRunning(boolean failIfAlreadyRunning)
    {
        this.failIfAlreadyRunning = failIfAlreadyRunning;
    }

}

