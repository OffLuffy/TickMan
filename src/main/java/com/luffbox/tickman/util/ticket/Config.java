package com.luffbox.tickman.util.ticket;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsonable;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.events.TMEventManager;
import com.luffbox.tickman.util.constants.ChangeType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import javax.annotation.Nonnull;
import java.io.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates all the Guild-specific data and allows for fetching and modifying it.
 * If the Guild is set to null, most methods that modify the data will fail.
 */
public class Config implements Jsonable {

	enum Field {
		// Paths to values in guild config
		GUILD_ID("guildId"),
		CMD_PREFIX("cmdPrefix"),
		ALLOW_INVITE("allowInvite"),
		EMBED_COLOR("embedColor"),
		DEPARTMENTS("departments"),

		// Paths to values in Departments subsection in guild config
		DEPT_ID("deptId"),
		DEPT_NAME("name"),
		DEPT_CATEGORY("ticketCategory"),
		DEPT_CHANNEL("supportChannel"),
		DEPT_ROLES("supportRoles"),
		DEPT_TICKETS("tickets"),

		// Paths to values in Tickets subsection in Departments config
		TICKET_ID("ticketId"),
		TICKET_DEPT("ticketDept"),
		TICKET_AUTHOR("author"),
		TICKET_CHANNEL("ticketChannel"),
		TICKET_SUBJECT("subject");

		public String path;
		Field(String path) { this.path = path; }
	}

	private final File outFile;
	private final TickMan tickman;

	private final Guild guild;
	private final Set<Department> departments = new HashSet<>();
	private String cmdPrefix = "!";
	private boolean allowInvite = false;
	private BigDecimal embedColor = BigDecimal.valueOf(0x33AAFF);

	private boolean isLoading = false;

	public Config(@Nonnull TickMan tickman, @Nonnull Guild guild) {
		this.tickman = tickman;
		this.guild = guild;
		outFile = new File(TickMan.GUILD_DATA, guild.getId() + ".json");
		load();
	}

	public TickMan tickManInst() { return tickman; }

	public void load() {
		isLoading = true;
		if (!outFile.exists()) {
			save();
			TMEventManager.configCreate(this);
		}

		try (FileReader readIn = new FileReader(outFile)) {
			JsonObject json = (JsonObject) Jsoner.deserialize(readIn);

			setCmdPrefix((String) json.get(Field.CMD_PREFIX.path));
			setAllowInvite((boolean) json.get(Field.ALLOW_INVITE.path));
			setEmbedColor((BigDecimal) json.get(Field.EMBED_COLOR.path));

			JsonObject depts = (JsonObject) json.get(Field.DEPARTMENTS.path);
			if (depts != null) {
				System.out.println("depts Json object not null");
				System.out.println("depts Json keySet = " + depts.keySet());
				if (depts.keySet().isEmpty()) {
					System.out.println("depts Json keySet is empty");
					System.out.println("Created: " + createDepartment("Support"));
					save();
				}
				for (String deptId : depts.keySet()) {
					System.out.println("Attempting to load Department with ID: " + deptId);
					try {
						Department dept = new Department(Long.parseLong(deptId),
								this, (String) json.get(Field.DEPT_NAME.path), (JsonObject) depts.get(deptId));
						System.out.println("Loaded: " + dept);
						departments.add(dept);
						save();
					} catch (Exception e) {
						System.err.println("Failed to load Department ID: " + deptId);
						e.printStackTrace();
					}
				}
			}
			isLoading = false;

		} catch (IOException e) {
			System.err.println("Failed to read guild data file for guild: " + guild.getName() + ", file: " + outFile.getName() + ", reason: " + e.getMessage());
		} catch (JsonException e) {
			System.err.println("Guild data file has invalid JSON for guild: " + guild.getName() + ", file: " + outFile.getName() + ", reason: " + e.getMessage());
		}
	}

	public void save() {
		try (FileWriter fw = new FileWriter(outFile)) {
			if (outFile.exists() || outFile.createNewFile()) { fw.write(toJson()); }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the Guild associated with this Config
	 * @return The Guild object assocaited with this Config. May be null if a default set of Config are used
//	 * @see Config#def()
	 */
	public Guild getGuild() { return guild; }

	/**
	 * Gets the Guild's current command prefix
	 * @return A String containing the Guild's current command prefix
	 */
	public String getCmdPrefix() { return cmdPrefix; }

	/**
	 * Indicates whether use of the invite command is enabled.
	 * If true, users can get a link to invite the bot to their own servers with the invite command.
	 * @return true if the invite command is enabled, false otherwise
	 */
	public boolean canInvite() { return allowInvite; }

	/**
	 * Gets the color that will be used for embeds created by the bot
	 * @return An int value representing the embed color
	 */
	public BigDecimal getEmbedColor() { return embedColor; }

	/**
	 * Gets a Set of the current Departments
	 * @return An immutable copy of the current Departments
	 */
	public Set<Department> getDepartments() { return Set.copyOf(departments); }

	/**
	 * Sets the Guild's command prefix
	 * @param cmdPrefix The prefix to require before commands
	 */
	public void setCmdPrefix(String cmdPrefix) {
		this.cmdPrefix = cmdPrefix;
		save();
		if (!isLoading) { TMEventManager.configChange(this, ChangeType.Config.PREFIX); }
	}

	/**
	 * Sets whether the Guild allows use of the invite command or not
	 * @param allow true if the invite command should be enabled, false otherwise
	 */
	public void setAllowInvite(boolean allow) {
		allowInvite = allow;
		save();
		if (!isLoading) { TMEventManager.configChange(this, ChangeType.Config.INVITE); }
	}

	public void setEmbedColor(BigDecimal color) {
		color = color.max(BigDecimal.valueOf(0x000000)).min(BigDecimal.valueOf(0xFFFFFF));
		embedColor = color;
		if (!isLoading) { TMEventManager.configChange(this, ChangeType.Config.COLOR); }
	}

	public EmbedBuilder newEmbed() {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(getEmbedColor().intValue());
		return embed;
	}

	public Department createDepartment(String name) {
		Department dept = new Department(TickMan.getSnowflake(), this, name, null);
		departments.add(dept);
		TMEventManager.departmentCreate(dept);
		return dept;
	}

	public void deleteDepartment(String deptId) {
		for (Department dept : getDepartments()) {
			if (dept.getId().equals(deptId)) {
				departments.remove(dept);
				TMEventManager.departmentDelete(dept);
			}
		}
	}

	public Ticket getTicketByChannel(TextChannel channel) {
		Ticket ticket = null;
		for (Department d : getDepartments()) {
			for (Ticket t : d.getTickets()) {
				if (t.getTicketChannel().equals(channel)) {
					ticket = t;
				}
			}
		}
		return ticket;
	}

	public Set<Ticket> getTicketsByMember(Member member) {
		Set<Ticket> foundTickets = new HashSet<>();
		for (Department d : getDepartments()) {
			for (Ticket t : d.getTickets()) {
				if (t.getAuthor().equals(member)) {
					foundTickets.add(t);
				}
			}
		}
		return foundTickets;
	}

	@Override
	public String toJson() {
		final StringWriter writable = new StringWriter();
		try { this.toJson(writable); } catch (final IOException e) { e.printStackTrace(); }
		return writable.toString();
	}

	@Override
	public void toJson(Writer writable) throws IOException {
		final JsonObject json = new JsonObject(new HashMap<>() {
			{
//				put(Field.GUILD_ID.path, guild.getId());
				put(Field.CMD_PREFIX.path, cmdPrefix);
				put(Field.ALLOW_INVITE.path, allowInvite);
				put(Field.EMBED_COLOR.path, embedColor);
				JsonObject depts = new JsonObject();
				for (Department dept : departments) { depts.put(dept.getId(), dept.toJson()); }
				put(Field.DEPARTMENTS.path, depts);
			}
		});
		json.toJson(writable);
	}
}
