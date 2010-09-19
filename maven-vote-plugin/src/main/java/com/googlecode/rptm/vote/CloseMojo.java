/*
 * Copyright 2010 Andreas Veithen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.rptm.vote;

import java.util.Arrays;
import java.util.Date;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal close
 * @aggregator true
 */
public class CloseMojo extends AbstractVoteMojo {
    public void execute() throws MojoExecutionException, MojoFailureException {
        VoteThread thread = loadState();
        for (String opinion : Arrays.asList("+1", "0", "-1")) {
            boolean hasVotes = false;
            for (boolean binding : Arrays.asList(true, false)) {
                int count = 0;
                StringBuilder names = new StringBuilder();
                for (Voter voter : thread.getVoters()) {
                    if (voter.isPmcMember() == binding) {
                        Date last = null;
                        Vote finalVote = null;
                        for (Vote vote : voter.getVotes()) {
                            if (last == null || last.compareTo(vote.getReceived()) < 0) {
                                finalVote = vote;
                            }
                        }
                        if (finalVote != null && finalVote.getOpinion().equals(opinion)) {
                            count++;
                            hasVotes = true;
                            if (count > 1) {
                                names.append(", ");
                            }
                            names.append(voter.getName());
                        }
                    }
                }
                if (count > 0) {
                    System.out.println(count + " " + (binding ? "binding" : "non binding") + " " + opinion + " votes: " + names + ".");
                }
            }
            if (!hasVotes) {
                System.out.println("No " + opinion + " votes.");
            }
        }
    }
}
