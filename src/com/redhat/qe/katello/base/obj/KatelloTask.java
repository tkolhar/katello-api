package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;
import javax.management.Attribute;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.simple.JSONObject;
import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloTask extends _KatelloObject{
	protected static Logger log = Logger.getLogger(KatelloTask.class.getName());

	@JsonProperty("task_type")
	public String task_type;
	
	@JsonProperty("task_owner_id")
	public String task_owner_id;
	
	@JsonProperty("organization_id")
	public String organization_id;
	
	@JsonProperty("user_id")
	public Long user_id;

	@JsonProperty("finish_time")
	public String finish_time;
	
	@JsonProperty("start_time")
	public String start_time;

	@JsonProperty("pending")
	public Boolean pending;

	@JsonProperty("uuid")
	public String uuid;
	
	@JsonProperty("updated_at")
	public String updated_at;

	@JsonProperty("state")
	public String state;

	@JsonProperty("parameters")
	public Object parameters;

	@JsonProperty("result")
	public JSONObject result;
	
	@JsonProperty("created_at")
	public String created_at;
	
	@JsonProperty("task_owner_type")
	public String task_owner_type;
	
	@JsonProperty("progress")
	public JSONObject progress;
	
	@JsonProperty("id")
	public Long id;

	public String org;
	
	public static final String CMD_STATUS = "task status";
	public static final String CMD_LIST = "task list -v";

	public static final String ERR_NOT_FOUND = "Could not find task [ %s ].";
	public static final String ERR_INVALID_STATE = "State '%s' not valid. It must be in [waiting, running, error, finished, canceled, timed_out].";

	public static final String REG_STATUS = "^.*Task Status.*UUID +: [0-9a-f-]+.*State +: .*Type +: .*Progress +: .*Start Time +: [0-9]{4}/[0-9]{2}/[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.*Finish Time +: [0-9]{4}/[0-9]{2}/[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.*Owner ID +: .*Owner Type +: .*$";

	public KatelloTask(){super();}

	public KatelloTask(KatelloCliWorker kcr, String org, String uuid) {
		this.kcr = kcr;
		this.org = org;
		this.uuid = uuid;
	}

	public SSHCommandResult status() {
		opts.clear();
		opts.add(new Attribute("uuid", uuid));
		return run(CMD_STATUS);
	}

	public SSHCommandResult list() {
		opts.clear();
		opts.add(new Attribute("org", org));
		return run(CMD_LIST);
	}

	public SSHCommandResult list(String state, String type) {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("state", state));
		opts.add(new Attribute("type", type));
		return run(CMD_LIST);
	}
}
