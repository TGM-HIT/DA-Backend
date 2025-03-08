package at.ac.tgm.controller;

import at.ac.tgm.TestConfiguration;
import at.ac.tgm.ad.Roles;
import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.api.AuthenticationApi;
import at.ac.tgm.config.AdLdapConfig;
import at.ac.tgm.dto.LoginRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@SpringBootTest(classes = {AuthenticationApi.class, AuthenticationController.class, AdLdapConfig.class})
@ContextConfiguration(classes = TestConfiguration.class) // Ensure dotenv is loaded
class AuthenticationControllerTest {
    
    public static final String TEACHER_USERNAME = "mpointner";
    public static final String STUDENT_USERNAME = "mmustermann";
    public static final String ADMIN_USERNAME = "${APPLICATION_ADMINS}";
    public static final String PASSWORD = "1234";
    public static final SimpleGrantedAuthority GROUP_STUDENT = new SimpleGrantedAuthority("schueler1AHIT");
    public static final SimpleGrantedAuthority GROUP_TEACHER = new SimpleGrantedAuthority("lehrer1AHIT");;
    public static final SimpleGrantedAuthority AUTHORITY_STUDENT = new SimpleGrantedAuthority(Roles.STUDENT);
    public static final SimpleGrantedAuthority AUTHORITY_TEACHER = new SimpleGrantedAuthority(Roles.TEACHER);
    public static final SimpleGrantedAuthority AUTHORITY_ADMIN = new SimpleGrantedAuthority(Roles.ADMIN);
    
    @MockitoBean
    private AuthenticationManager authenticationManager;
    
    @MockitoBean
    private Environment env;
    
    @MockitoBean
    private SecurityContextRepository securityContextRepository;
    
    @MockitoBean
    private UserService userService;
    
    @Autowired
    private AuthenticationApi authenticationController;
    
    @Autowired
    private UserDetailsContextMapper userDetailsContextMapper;
    
    private UserEntry studentEntry;
    private UserEntry teacherEntry;
    private UserEntry adminEntry;
    
    @Value("${admins}")
    private List<String> admins;
    
    @BeforeEach
    void setUpBeforeClass() throws Exception {
        studentEntry = buildStudentUserEntry();
        Mockito.when(userService.findBysAMAccountName(Mockito.contains(STUDENT_USERNAME))).thenReturn(Optional.of(studentEntry));
        
        teacherEntry = buildTeacherUserEntry();
        Mockito.when(userService.findBysAMAccountName(Mockito.contains(TEACHER_USERNAME))).thenReturn(Optional.of(teacherEntry));
        
        adminEntry = buildAdminUserEntry();
        Mockito.when(userService.findBysAMAccountName(Mockito.contains(ADMIN_USERNAME))).thenReturn(Optional.of(adminEntry));
        
        Mockito.when(env.matchesProfiles(Mockito.anyString())).thenReturn(true);
        
        Mockito.when(authenticationManager.authenticate(Mockito.any())).thenReturn(new UsernamePasswordAuthenticationToken(teacherEntry.getSAMAccountName(), PASSWORD, List.of(GROUP_TEACHER)));
    }
    
    @Test
	void testLoginSimulateUser() {
        System.out.println("System.getProperty(admins): " + System.getProperty("APPLICATION_ADMINS"));
        System.out.println("System.getProperty(admins): " + admins);
        //assertThat(authenticationController).isNotNull();
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUsername(TEACHER_USERNAME);
        loginRequestDto.setPassword(PASSWORD);
        loginRequestDto.setSimulate(true);
        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        ResponseEntity<Authentication> authenticationResponseEntity = authenticationController.login(loginRequestDto, httpServletRequest, httpServletResponse);
        Authentication authentication = authenticationResponseEntity.getBody();
        System.out.println(authentication);
        assert authentication != null;
        assert authentication.isAuthenticated();
        assert authentication.getPrincipal().equals(TEACHER_USERNAME);
        assert authentication.getAuthorities().contains(GROUP_TEACHER);
        assert authentication.getAuthorities().contains(AUTHORITY_TEACHER);
	}
    
    @Test
    void testLoginRealUser() {
        //assertThat(authenticationController).isNotNull();
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUsername(TEACHER_USERNAME);
        loginRequestDto.setPassword(PASSWORD);
        loginRequestDto.setSimulate(false);
        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        ResponseEntity<Authentication> authenticationResponseEntity = authenticationController.login(loginRequestDto, httpServletRequest, httpServletResponse);
        Authentication authentication = authenticationResponseEntity.getBody();
        System.out.println(authentication);
        assert authentication != null;
        assert authentication.isAuthenticated();
        assert authentication.getPrincipal().equals(TEACHER_USERNAME);
        assert authentication.getAuthorities().contains(GROUP_TEACHER);
    }
    
    @Test
    void testSchuelerMapping() {
        List<SimpleGrantedAuthority> authoritiesWithGroups = new ArrayList<>();
        authoritiesWithGroups.add(GROUP_STUDENT);
        UserDetails userDetails = userDetailsContextMapper.mapUserFromContext(new DirContextAdapter(studentEntry.getId()), studentEntry.getSAMAccountName(), authoritiesWithGroups);
        assert userDetails.getAuthorities().contains(AUTHORITY_STUDENT);
    }
    
    @Test
    void testTeacherMapping() {
        List<SimpleGrantedAuthority> authoritiesWithGroups = new ArrayList<>();
        authoritiesWithGroups.add(GROUP_TEACHER);
        UserDetails userDetails = userDetailsContextMapper.mapUserFromContext(new DirContextAdapter(teacherEntry.getId()), teacherEntry.getSAMAccountName(), authoritiesWithGroups);
        assert userDetails.getAuthorities().contains(AUTHORITY_TEACHER);
    }
    
    @Test
    void testAdminMapping() {
        List<SimpleGrantedAuthority> authoritiesWithGroups = new ArrayList<>();
        authoritiesWithGroups.add(GROUP_TEACHER);
        UserDetails userDetails = userDetailsContextMapper.mapUserFromContext(new DirContextAdapter(adminEntry.getId()), adminEntry.getSAMAccountName(), authoritiesWithGroups);
        assert userDetails.getAuthorities().contains(AUTHORITY_TEACHER);
        assert userDetails.getAuthorities().contains(AUTHORITY_ADMIN);
    }
    
    @Test
    void testLogout() {
        MockHttpSession session = new MockHttpSession();
        assert !session.isInvalid();
        ResponseEntity<String> responseEntity = authenticationController.logout(session);
        assert responseEntity.getBody().equals("User logged out successfully");
        assert session.isInvalid();
    }
    
    
    private UserEntry buildStudentUserEntry() throws InvalidNameException {
        UserEntry entry = new UserEntry();
        entry.setId(new LdapName("CN=Max Mustermann,OU=Lehrer,OU=People,OU=tgm,DC=tgm,DC=ac,DC=at"));
        entry.setSAMAccountName(TEACHER_USERNAME);
        HashSet<Name> memberOf = new HashSet<>();
        memberOf.add(new LdapName("CN=schueler1AHIT,OU=HIT,OU=Groups,OU=tgm,DC=tgm,DC=ac,DC=at"));
        entry.setMemberOf(memberOf);
        return entry;
    }
    
    private UserEntry buildTeacherUserEntry() throws InvalidNameException {
        UserEntry entry = new UserEntry();
        entry.setId(new LdapName("CN=Michael Pointner,OU=Lehrer,OU=People,OU=tgm,DC=tgm,DC=ac,DC=at"));
        entry.setSAMAccountName(TEACHER_USERNAME);
        HashSet<Name> memberOf = new HashSet<>();
        memberOf.add(new LdapName("CN=lehrer1AHIT,OU=Groups,OU=tgm,DC=tgm,DC=ac,DC=at"));
        entry.setMemberOf(memberOf);
        return entry;
    }
    
    private UserEntry buildAdminUserEntry() throws InvalidNameException {
        UserEntry entry = new UserEntry();
        entry.setId(new LdapName("CN=Admin Admin,OU=Lehrer,OU=People,OU=tgm,DC=tgm,DC=ac,DC=at"));
        entry.setSAMAccountName(ADMIN_USERNAME);
        HashSet<Name> memberOf = new HashSet<>();
        memberOf.add(new LdapName("CN=lehrer1AHIT,OU=Groups,OU=tgm,DC=tgm,DC=ac,DC=at"));
        entry.setMemberOf(memberOf);
        return entry;
    }
}
